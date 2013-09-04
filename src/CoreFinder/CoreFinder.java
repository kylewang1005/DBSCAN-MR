package CoreFinder;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class CoreFinder {
	
	public CoreFinder() {}
	
	public void run(double epsilon, int mi, String input_file, 
			String order_file, String cores_file) throws IOException {
		Path p = new Path(cores_file);
		String similarities_file = 
				new String(p.getParent().toString()+"/similarities-tmp.txt");
		//String similarities_file = "/user/helio/outputs/similarities-tmp.txt";
		findSimilars(epsilon, input_file, order_file, similarities_file);
		// May delete similarities_file here
		defineCores(mi, similarities_file, cores_file);
		Configuration conf = new Configuration();
		conf.set("fs.default.name","hdfs://127.0.0.1:54310/");		
		FileSystem dfs = FileSystem.get(conf);
		Path file = new Path(similarities_file);
		if (dfs.exists(file)) dfs.delete(file, true);
	}
	
	private void findSimilars(double epsilon, String input_file, 
			String order_file, String output_file) throws IOException {
		Configuration conf = new Configuration();
		// Similarity threshold
		conf.set("epsilon", String.valueOf(epsilon));
		// Minimum neighbor count for core
		//conf.set("mi", String.valueOf(mi));
		conf.set("job.customorder.path", order_file);
		conf.set("fs.default.name","hdfs://127.0.0.1:54310/");		
		FileSystem dfs = FileSystem.get(conf);
		
        Job job = new Job(conf, "Similarities");
        
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
               
        job.setMapperClass(SimilarityMapper.class);
        job.setReducerClass(SimilarityReducer.class);
       
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        
        FileInputFormat.addInputPath(job, new Path(input_file));
		Path output = new Path(output_file);
		if (dfs.exists(output)) dfs.delete(output, true);
        FileOutputFormat.setOutputPath(job, output);
            
        try {
			job.waitForCompletion(true);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void defineCores(int mi, String similarities_file, String output_file) throws IOException {
		Configuration conf = new Configuration();
		// Minimum neighbor count for core
		conf.set("mi", String.valueOf(mi));
		conf.set("fs.default.name","hdfs://127.0.0.1:54310/");		
		FileSystem dfs = FileSystem.get(conf);
		
        Job job = new Job(conf, "CoreFinder");
        
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
               
        job.setMapperClass(CoreFinderMapper.class);
        job.setReducerClass(CoreFinderReducer.class);
       
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        
        FileInputFormat.addInputPath(job, new Path(similarities_file));
		Path output = new Path(output_file);
		if (dfs.exists(output)) dfs.delete(output, true);
        FileOutputFormat.setOutputPath(job, output);
            
        try {
			job.waitForCompletion(true);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}