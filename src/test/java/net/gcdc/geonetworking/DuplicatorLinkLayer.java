package net.gcdc.geonetworking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DuplicatorLinkLayer {

    private final boolean hasEthernetHeader;

    public DuplicatorLinkLayer(boolean hasEthernetHeader) {
        this.hasEthernetHeader = hasEthernetHeader;
    }

    public DuplicatorLinkLayer() {
        this(false);
    }

    public static abstract class QueuingLinkLayer implements LinkLayer {

        BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

        @Override
        public byte[] receive() throws IOException, InterruptedException { return queue.take(); }

        public void addToQueue(byte[] payload) throws InterruptedException { queue.put(payload); }
    }

    Collection<QueuingLinkLayer> links = new ArrayList<>();

    public void sendToAll(byte[] payload) throws InterruptedException {
        for (QueuingLinkLayer l : links) { l.addToQueue(payload.clone()); }
    }

    public LinkLayer get() {
        QueuingLinkLayer link = new QueuingLinkLayer() {
            @Override public void send(byte[] payload) throws IOException {
                try { sendToAll(payload); } catch (InterruptedException e) { e.printStackTrace(); }
            }

            @Override public void close() { }

            @Override public boolean hasEthernetHeader() { return hasEthernetHeader; }
        };
        links.add(link);
        return link;
    }

}
