package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private final long interval;
    private final int requestLimit;
    private final AtomicInteger requestCount;
    private volatile long startTime;
    CrptApi(TimeUnit timeUnit, int requestLimit){
        this.interval = timeUnit.toMillis(1);
        this.requestLimit = requestLimit;
        this.requestCount = new AtomicInteger(0);
        this.startTime = System.currentTimeMillis();
    }
    public synchronized void createDocument(DocumentForCreation documentForCreation, String signature) throws IOException, InterruptedException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTime >= interval) {
            requestCount.set(1);
            startTime = currentTime;
        } else {
            int currentCount = requestCount.incrementAndGet();
            if (currentCount > requestLimit) {
                long sleepTime = interval - (currentTime - startTime);
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
                var objectMapper = new ObjectMapper();
                String requestBody = objectMapper.writeValueAsString(documentForCreation);
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .header("signature", signature)
                        .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            }
        }
    }

    public static class DocumentForCreation {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private Date production_date;
        private String production_type;
        private Product[] products;
        private Date reg_date;
        private String reg_number;

        public static class Description {
            private String participantInn;
            public Description(String participantInn) {
                this.participantInn = participantInn;
            }
        }

        public static class Product {
            private String certificate_document;
            private Date certificate_document_date;
            private String certificate_document_number;
            private String owner_inn;
            private String producer_inn;
            private Date production_date;
            private String tnved_code;
            private String uit_code;
            private String uitu_code;
            public Product(String certificate_document,
                           Date certificate_document_date,
                           String certificate_document_number,
                           String owner_inn,
                           String producer_inn,
                           Date production_date,
                           String tnved_code,
                           String uit_code,
                           String uitu_code) {
                this.certificate_document = certificate_document;
                this.certificate_document_date = certificate_document_date;
                this.certificate_document_number = certificate_document_number;
                this.owner_inn = owner_inn;
                this.producer_inn = producer_inn;
                this.production_date = production_date;
                this.tnved_code = tnved_code;
                this.uit_code = uit_code;
                this.uitu_code = uitu_code;
            }
        }
        public DocumentForCreation(Description description,
                                   String doc_id,
                                   String doc_status,
                                   String doc_type,
                                   boolean importRequest,
                                   String owner_inn,
                                   String participant_inn,
                                   String producer_inn,
                                   Date production_date,
                                   String production_type,
                                   Product[] products,
                                   Date reg_date,
                                   String reg_number) {
            this.description = description;
            this.doc_id = doc_id;
            this.doc_status = doc_status;
            this.doc_type = doc_type;
            this.importRequest = importRequest;
            this.owner_inn = owner_inn;
            this.participant_inn = participant_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.production_type = production_type;
            this.products = products;
            this.reg_date = reg_date;
            this.reg_number = reg_number;
        }
    }
}
