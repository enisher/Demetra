package ua.org.enishlabs.demetra.genetic.distributed;

import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.Decoder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.serializer.JavaSerialization;
import org.encog.ml.data.basic.BasicMLDataSet;

import java.io.*;

/**
 * @author EniSh
 *         Date: 04.05.12
 */
public class TrainingSetProvider {
    private Configuration configuration;
    private Path path = new Path("temp/learningSet.ser");

    public TrainingSetProvider() {
        configuration = new Configuration();
//        configuration.set("io.serialization", JavaSerialization.class.getName());
    }

    public void save(BasicMLDataSet dataSet) {
        try {
            final FileSystem fs = FileSystem.get(configuration);
            final FSDataOutputStream file = fs.create(path, true);
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
            oos.writeObject(dataSet);
            oos.close();
            final byte[] buffer = byteArrayOutputStream.toByteArray();
            file.writeInt(buffer.length);
            file.write(buffer);
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BasicMLDataSet load() {
        try {
            final FileSystem fs = FileSystem.get(configuration);
            final FSDataInputStream file = fs.open(path);
            final int len = file.readInt();
            final byte[] buffer = new byte[len];
            file.read(buffer);
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            final ObjectInputStream oos = new ObjectInputStream(byteArrayInputStream);
            final BasicMLDataSet result = (BasicMLDataSet) oos.readObject();
            oos.close();
            file.close();
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
