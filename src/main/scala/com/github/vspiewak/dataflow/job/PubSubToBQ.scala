package com.github.vspiewak.dataflow.job

import com.github.vspiewak.dataflow.example.ExampleData
import com.github.vspiewak.dataflow.options.ExampleOptions
import com.github.vspiewak.dataflow.options.ExampleOptions._
import com.github.vspiewak.dataflow.utils.DataflowExampleUtils
import com.google.api.services.bigquery.model.{TableFieldSchema, TableSchema}
import com.google.cloud.dataflow.sdk.io.BigQueryIO.Write.WriteDisposition
import com.spotify.scio._
import com.spotify.scio.bigquery._
import org.joda.time.{Duration, Instant}

import scala.collection.JavaConverters._

object PubSubToBQ {

  val RAND_RANGE = 7200000
  val WINDOW_SIZE = 1

  val schema = new TableSchema().setFields(List(
    new TableFieldSchema().setName("message").setType("STRING"),
    new TableFieldSchema().setName("processed_at").setType("TIMESTAMP")
  ).asJava)

  def main(cmdlineArgs: Array[String]): Unit = {
    // set up example wiring
    val (opts, args) = ScioContext.parseArguments[ExampleOptions](cmdlineArgs)
    val dataflowUtils = new DataflowExampleUtils(opts)
    dataflowUtils.setup()

    val sc = ScioContext(opts)

    val inputFile = args.optional("inputFile")
    val windowSize = Duration.standardMinutes(
      args.optional("windowSize").map(_.toLong).getOrElse(WINDOW_SIZE))

    // initialize input
    val input = if (opts.isStreaming) {
      sc.pubsubSubscription(opts.getPubsubSubscription)
    } else {
      sc
        .textFile(inputFile.getOrElse(ExampleData.KING_LEAR))
        .timestampBy {
          _ => new Instant(System.currentTimeMillis() - (scala.math.random * RAND_RANGE).toLong)
        }
    }

    input
       .withFixedWindows(windowSize)  // apply windowing logic
       .toWindowed  // convert to WindowedSCollection
       .map { wv =>
         wv.copy(value = TableRow(
           "message" -> wv.value,
           "processed_at" -> Timestamp(wv.timestamp)))
       }
       .toSCollection  // convert back to normal SCollection
       .saveAsBigQuery(bigQueryTable(opts), schema, WriteDisposition.WRITE_APPEND)

    val result = sc.close()

    // set up Pubsub topic from input file in an injector pipeline
    inputFile.foreach { f =>
      dataflowUtils.runInjectorPipeline(f, opts.getPubsubTopic)
    }

    // CTRL-C to cancel the streaming pipeline
    dataflowUtils.waitToFinish(result.internal)

  }

}
