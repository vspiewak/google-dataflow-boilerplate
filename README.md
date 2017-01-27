google-dataflow-boilerplate
===========================

Google Cloud DataFlow boilerplate

Use Scala with Spotify SCIO project

    sbt "run-main \
         com.github.vspiewak.dataflow.job.WordCount \
         --input=README.md \
         --output=wc"

    sbt "run-main com.github.vspiewak.dataflow.job.PubSubToBQ \
         --runner=DataflowPipelineRunner \
         --streaming=true \
         --project=<project_id>  \
         --zone="<zone_name>" \
         --stagingLocation=gs://<bucket_name>/dataflow/staging \
         --pubsubTopic=projects/<project_id>/topics/<topic_name> \
         --pubsubSubscription=projects/<project_id>/subscriptions/<subscription_name> \
         --bigQueryDataset=<bigquery_dataset_name> \
         --bigQueryTable=<big_query_table_name> \
         --deleteTopicAtShutdown=false \
         --deleteSubscriptionAtShutdown=false \
         --maxNumWorkers=2"
