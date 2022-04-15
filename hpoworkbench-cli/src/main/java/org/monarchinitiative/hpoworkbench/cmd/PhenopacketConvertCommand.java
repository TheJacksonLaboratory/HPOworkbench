package org.monarchinitiative.hpoworkbench.cmd;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.ga4gh.vrsatile.v1.GeneDescriptor;
import org.ga4gh.vrsatile.v1.VariationDescriptor;
import org.ga4gh.vrsatile.v1.VcfRecord;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.phenopackets.phenotools.builder.builders.GenomicInterpretationBuilder;
import org.phenopackets.phenotools.builder.builders.InterpretationBuilder;
import org.phenopackets.phenotools.builder.builders.VariationDescriptorBuilder;
import org.phenopackets.phenotools.converter.converters.PhenopacketConverter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.phenopackets.schema.v1.core.Disease;
import org.phenopackets.schema.v1.core.OntologyClass;
import org.phenopackets.schema.v1.core.VcfAllele;
import org.phenopackets.schema.v2.Phenopacket;
import org.phenopackets.schema.v2.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "convert",
        mixinStandardHelpOptions = true,
        description = "Convert phenopackete from v1 to v2 with genotype info")
public class PhenopacketConvertCommand implements Callable<Integer> {
    private final Logger logger = LoggerFactory.getLogger(PhenopacketConvertCommand.class);
    @CommandLine.Option(names={"--phenopacket"},
            required = true,
            description = "path to a v1 phenopacket")
    private String phenopacket;

    @Override
    public Integer call() throws InvalidProtocolBufferException {
        JSONParser parser = new JSONParser();
        logger.trace("Importing Phenopacket: " + phenopacket);
        org.phenopackets.schema.v1.Phenopacket v1phenopacket = null;
        try {
            Object obj = parser.parse(new FileReader(phenopacket));
            JSONObject jsonObject = (JSONObject) obj;
            String phenopacketJsonString = jsonObject.toJSONString();
            org.phenopackets.schema.v1.Phenopacket.Builder phenoPacketBuilder = org.phenopackets.schema.v1.Phenopacket.newBuilder();
            com.google.protobuf.util.JsonFormat.parser().merge(phenopacketJsonString, phenoPacketBuilder);
            v1phenopacket = phenoPacketBuilder.build();
        } catch (IOException  | ParseException e) {
            e.printStackTrace();
        }
        logger.info("Done ingesting V1 Phenopacket");
        org.phenopackets.schema.v2.Phenopacket v2Phenopacket = PhenopacketConverter.toV2Phenopacket(v1phenopacket);
        System.out.println(JsonFormat.printer().print(v1phenopacket));
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        System.out.println(JsonFormat.printer().print(v2Phenopacket));
        V1GeneVariantInfo v1GeneVariantInfo = new V1GeneVariantInfo(v1phenopacket);
        Interpretation interpretation = v1GeneVariantInfo.getInterpretation();
        Phenopacket.Builder extendedBuilder = Phenopacket.newBuilder(v2Phenopacket).addInterpretations(interpretation);
        org.phenopackets.schema.v2.Phenopacket v2Phenopacket2 = extendedBuilder.build();
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

        System.out.println(JsonFormat.printer().print(v2Phenopacket2));
        return 0;
    }




    static class V1GeneVariantInfo {
        private final List<org.phenopackets.schema.v1.core.Variant> variantList;

        private final GeneDescriptor geneDescriptor;
        private final Disease v1disease;

        public V1GeneVariantInfo( org.phenopackets.schema.v1.Phenopacket v1pp) {
            List<org.phenopackets.schema.v1.core.Gene> geneList = v1pp.getGenesList();
            if  (geneList.size() != 1) {
                throw new PhenolRuntimeException("Expectiing to find one gene but got " + geneList.size());
            }
            org.phenopackets.schema.v1.core.Gene gene = geneList.get(0);
            geneDescriptor = GeneDescriptor.newBuilder().setSymbol(gene.getSymbol()).setValueId(gene.getId()).build();
            variantList = v1pp.getVariantsList();
            List<Disease> v1diseaseList = v1pp.getDiseasesList();
            if  (v1diseaseList.size() != 1) {
                throw new PhenolRuntimeException("Expectiing to find one disease but got " + v1diseaseList.size());
            }
            v1disease = v1diseaseList.get(0);


        }

        public Interpretation getInterpretation() {
            InterpretationBuilder builder = InterpretationBuilder.create("id", Interpretation.ProgressStatus.COMPLETED);
            String id = v1disease.getTerm().getId();
            String label = v1disease.getTerm().getLabel();
            Diagnosis.Builder dxBuilder = Diagnosis.newBuilder()
                    .setDisease(org.phenopackets.schema.v2.core.OntologyClass.newBuilder().setId(id).setLabel(label));
            int i = 1;
            for (var v1variant : this.variantList) {
                String identifier = "id" + i;
                i++;
                GenomicInterpretation gi = getGenomicInterpretation(identifier, v1variant);
                dxBuilder.addGenomicInterpretations(gi);
            }
            builder.diagnosis(dxBuilder.build());
            return builder.build();
        }


        /**
         * For the purposes of this conversion, we assume variants are pathogenic and our interpretation is causative.
         * @param id
         * @param v1variant
         * @return
         */
        public GenomicInterpretation getGenomicInterpretation(String id, org.phenopackets.schema.v1.core.Variant v1variant) {
            VariationDescriptor variationDescriptor = getVariationDescriptor(v1variant);
            VariantInterpretation variantInterpretation = VariantInterpretation.newBuilder()
                    .setAcmgPathogenicityClassification(AcmgPathogenicityClassification.PATHOGENIC)
                    .setVariationDescriptor(variationDescriptor)
                    .build();
            return GenomicInterpretationBuilder.genomicInterpretation(id,
                            GenomicInterpretation.InterpretationStatus.
                                    CAUSATIVE, variantInterpretation);
        }


        private VariationDescriptor getVariationDescriptor(org.phenopackets.schema.v1.core.Variant v1variant) {
            if (! v1variant.hasVcfAllele()) {
                throw new PhenolRuntimeException("Only VcfAlle variants are supported: " + v1variant);
            }
            VcfAllele vcfAllele = v1variant.getVcfAllele();
            String assembly = vcfAllele.getGenomeAssembly();
            String chrom = vcfAllele.getChr();
            int pos = vcfAllele.getPos();
            String ref = vcfAllele.getRef();
            String alt = vcfAllele.getAlt();

            VcfRecord vcfRecord = VcfRecord.newBuilder()
                    .setChrom(chrom)
                    .setPos(pos)
                    .setRef(ref)
                    .setAlt(alt)
                    .setGenomeAssembly(assembly)
                    .build();

            VariationDescriptorBuilder builder = VariationDescriptorBuilder.create("id");
            builder.geneContext(this.geneDescriptor);
            builder.vcfVecord(vcfRecord);
            // this must be a sequence ontology class
            OntologyClass zygosity = v1variant.getZygosity();
            switch (zygosity.getLabel()) {
                case "homozygous" -> builder.homozygous();
                case "heteroyzgous" -> builder.heterozygous();
                case "hemizygous" -> builder.hemizygous();
                default -> throw new PhenolRuntimeException("Unrecognized zygosity " + zygosity.getLabel());
            }
            return builder.build();
        }

    }



}
