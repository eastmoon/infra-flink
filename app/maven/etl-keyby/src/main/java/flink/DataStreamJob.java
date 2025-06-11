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

// Data Source libraries
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

// Data stream operators libraries
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

// Package class
import flink.WordCount;

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

		// Create a DataStream
		DataStream<String> ds = env.fromElements(
			"Good medicine tastes bitter.",
			"A friend in need is a friend indeed.",
			"A little learning is a dangerous thing."
		);

		// Process with DataStream
		//// Split word
		DataStream<WordCount> wordArr = ds.flatMap(new Splitter());
		//// Group by with "word", and sum with "count"
		SingleOutputStreamOperator<WordCount> rs = wordArr.keyBy("word").sum("count");

		// Output process content with FileSink
		rs.print();

		// Execute program, beginning computation.
		env.execute("Flink demo application - ETL keyBy function");
	}

	public static class Splitter implements FlatMapFunction<String, WordCount> {
	    @Override
	    public void flatMap(String value, Collector<WordCount> out) throws Exception {
	        for ( String word : value.split(" ") ) {
							out.collect(new WordCount(word));
					}
	    }
	}

}
