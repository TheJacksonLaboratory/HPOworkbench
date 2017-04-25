package org.monarch.hpoapi.util;

/**
 * This is an implementation of a tiny queue avoiding bloat as much as possible.
 * It minimizes the allocation of entries as it marks the memory of
 * dequeued elements as unused. This should unload the garbage collector.
 * Only use it for temporary queues of which elements are inserted or removed
 * in high frequency.
 *
 * @author Sebastian Bauer
 */
public class TinyQueue<Type>
{
    /** Structure used for the embedding of the elements */
    static final private class TinyElement<Type>
    {
        TinyElement<Type> next;
        Type t;
    }

    /** Head of the queue */
    TinyElement<Type> head;

    /** Tail of the queue */
    TinyElement<Type> tail;

    /** Head of the free list */
    TinyElement<Type> headOfFree;

    /**
     * Internal function to allocate a new element. Unless there are no
     * free elements left a new one is allocated. Note that the t field
     * of the returned element is garbage.
     *
     * @return the new element.
     */
    private TinyElement<Type> allocateElement()
    {
        if (headOfFree != null)
        {
            TinyElement<Type> te = headOfFree;
            headOfFree = te.next;
            te.next = null;
            return te;
        }
        return new TinyElement<Type>();
    }

    /**
     * Deallocates the given element. In fact, it is pushed
     * on top of the free list.
     * @param t
     */
    private void deallocateElement(TinyElement<Type> t)
    {
        t.next = headOfFree;
        headOfFree = t;
    }

    /**
     * Offers a new element for the queue (i.e., appends it)
     *
     * @param t
     */
    public void offer(Type t)
    {
        TinyElement<Type> te = allocateElement();
        te.t = t;
        if (head == null)
        {
            head = tail = te;
        } else
        {
            tail.next = te;
            tail = te;
        }
    }

    /**
     * Polls the first element of the queue (i.e., removes it).
     *
     * @return the first element.
     */
    public Type poll()
    {
        TinyElement<Type> te = head;
        head = te.next;
        if (head == null) tail = null;
        deallocateElement(te);
        return te.t;
    }

    /**
     * @return whether queue is empty or not.
     */
    public boolean isEmpty()
    {
        return head == null;
    }
}