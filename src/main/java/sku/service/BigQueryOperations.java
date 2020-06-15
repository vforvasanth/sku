package sku.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;

import bean.Sku;

@Service
public class BigQueryOperations {

    public List<Sku> lookup(String skuIn) {
        List<Sku> skus = new ArrayList<>();
        String projectId = "dataflow-265804";

        try {
            GoogleCredentials credentials = ServiceAccountCredentials
                    .fromStream(getClass().getResourceAsStream("/sku-265804-e594ae1c315f.json"));

            // Instantiate a client.
            BigQuery bigquery = BigQueryOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build()
                    .getService();

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(
                    "SELECT * FROM `dataflow-265804.sku.sku` WHERE SKU='" + skuIn + "' ORDER BY price LIMIT 2")
                    .setUseLegacySql(false)
                    .build();

            // Create a job ID so that we can safely retry.
            JobId jobId = JobId.of(UUID.randomUUID().toString());
            Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

            queryJob = queryJob.waitFor();

            // Check for errors
            if (queryJob == null) {
                throw new RuntimeException("Job no longer exists");
            } else if (queryJob.getStatus().getError() != null) {
                throw new RuntimeException(queryJob.getStatus().getError().toString());
            }

            // Get the results.
            TableResult result = queryJob.getQueryResults();

            for (FieldValueList row : result.iterateAll()) {
                Sku sku = new Sku();

                sku.item = row.get("Item").getStringValue();
                sku.detail1 = row.get("Attr_1").getStringValue();
                sku.detail2 = row.get("Attr_2").getStringValue();
                sku.detail3 = row.get("Attr_3").getStringValue();
                sku.sku = row.get("SKU").getStringValue();
                sku.store = row.get("Store").getStringValue();
                sku.price = Double.parseDouble(row.get("Price").getStringValue());
                sku.stock = Integer.parseInt(row.get("Stock").getStringValue());

                skus.add(sku);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return skus;
    }

}
