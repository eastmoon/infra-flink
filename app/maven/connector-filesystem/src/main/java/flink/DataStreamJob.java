/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flink;

// Flink librares
//// Stream environment libraries
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//// Data stream operators libraries
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
//// Operator libraries
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
//// File connector - source libraries
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineFormat;
//// File connector - sink libraries
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;

// Time libraries
import java.time.Duration;

/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class DataStreamJob {

	public static void main(String[] args) throws Exception {
		// Sets up the execution environment, which is the main entry point
		// to building Flink applications.
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// Create a data source
		final FileSource<String> source = FileSource.forRecordStreamFormat(new TextLineFormat(), new Path("/cache/input"))
				.monitorContinuously(Duration.ofSeconds(1L))
		  	.build();

		// Create a data sink
		final FileSink<String> sink = FileSink.forRowFormat(new Path("/cache/output"), new SimpleStringEncoder<String>("UTF-8"))
				.withRollingPolicy(
						DefaultRollingPolicy.builder()
								.withRolloverInterval(Duration.ofMinutes(15))
								.withInactivityInterval(Duration.ofMinutes(5))
								.build())
				.withOutputFileConfig(
						OutputFileConfig
								.builder()
								.withPartPrefix("Flink_")
								.withPartSuffix(".dat")
								.build())
				.build();

		// Create a DataStrea
		final DataStream<String> ds = env.fromSource(source, WatermarkStrategy.noWatermarks(), "file-source");

		// Process with DataStream
		final DataStream<String> rs = ds.flatMap(new Calculate());

		// Output process content with FileSink
		rs.print();
		rs.sinkTo(sink);

		// Execute program, beginning computation.
		env.execute("Flink demo application - connector-filesystem");
	}

	public static class Calculate implements FlatMapFunction<String, String> {
	    @Override
	    public void flatMap(String value, Collector<String> out) throws Exception {
	        out.collect( value.replaceAll("[0-9]", "_") );
	    }
	}
}
