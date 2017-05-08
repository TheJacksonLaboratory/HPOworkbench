package org.monarch.hpoapi;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Created by peter on 08.05.17.
 */
public class HPOAPI {




    public static void main(String[] args){
        System.out.println("HPOAPI");
        // Setup command line parser
        ArgumentParser parser = ArgumentParsers.newArgumentParser("hpoapi-cli");
        parser.version(getVersion());
        parser.addArgument("--version").help("Show HPOAPI version").action(Arguments.version());
        parser.description("HPOAPI CLI performs a series of Human Phenotype Ontology (HPO) and HPO-annotation tasks.");
        Subparsers subParsers = parser.addSubparsers();
    }

    public static String getVersion() {
        return HPOAPI.class.getPackage().getSpecificationVersion();
    }
}
