package org.cogcomp.nlp.cooccurrence.wikipedia;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.cogcomp.nlp.cooccurrence.util.CoocUtil;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ExtractWikiEntities {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java ExtractWikiEntities [list-of-path-to-records] [out-path] [num-threads]");
            System.exit(1);
        }

        String inList = args[0];
        String outPath = args[1];
        int threads = Integer.parseInt(args[2]);

        ExecutorService pool = CoocUtil.getBoundedThreadPool(threads);
        AtomicInteger count = new AtomicInteger(0);

        // Read path to Records from a file
        try {
            List<String> paths = LineIO.read(inList);
            BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
            for (String path: paths) {
                pool.execute(new ExtractWikiEntities().new Processor(path, bw, count));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private class Processor implements Runnable {

        private String recPath;
        private final BufferedWriter out;
        private final AtomicInteger count;

        public Processor(String recPath, BufferedWriter out, AtomicInteger count) {
            this.recPath = recPath;
            this.out = out;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                byte[] recBytes = IOUtils.toByteArray(new FileInputStream(recPath));
                Record rec = deserializeRecordFromBytes(recBytes);
                Map<String, Labeling> views = rec.getLabelViews();
                if (views.containsKey("wikifier")) {
                    List<Span> spans = views.get("wikifier").getLabels();
                    String links = spans.stream()
                            .map(Span::getLabel)
                            .filter(lbl -> !lbl.equals("UNMAPPED"))
                            .map(s -> {
                                String[] parts = s.split("/");
                                return parts[parts.length - 1];
                            })
                            .collect(Collectors.joining(" "));

                    StringBuilder line = new StringBuilder()
                            .append(FilenameUtils.getBaseName(recPath))
                            .append('\t')
                            .append(links);

                    synchronized (out) {
                        out.write(line.toString());
                        out.newLine();
                        int _count = count.incrementAndGet();
                        if (_count % 100 == 0) {
                            System.out.println("Processed:\t" + _count);
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Record deserializeRecordFromBytes(byte[] bytes) throws TException {
        TDeserializer deserializer = new TDeserializer(
                new TBinaryProtocol.Factory());
        Record r = new Record();
        deserializer.deserialize(r, bytes);
        return r;
    }


}
