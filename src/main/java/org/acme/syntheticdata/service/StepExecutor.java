package org.acme.syntheticdata.service;

import com.google.genai.Chat;

import java.util.function.IntFunction;

@FunctionalInterface
public interface StepExecutor {
    String execute(String label, String prompt) throws Exception;

    static String executeBatchedStep(
            Chat chat,
            String stepName,
            int totalRequested,
            int maxBatchSize,
            IntFunction<String> promptSupplier,
            StepExecutor executor) throws Exception {

        StringBuilder sb = new StringBuilder();

        if (totalRequested <= 0) {
            return "";
        }

        if (totalRequested <= maxBatchSize) {
            String prompt = promptSupplier.apply(totalRequested);
            if (prompt != null) {
                sb.append(executor.execute(stepName, prompt)).append("\n\n\n");
            }
            return sb.toString();
        }

        int fullBatches = totalRequested / maxBatchSize;
        int remainder = totalRequested % maxBatchSize;

        for (int i = 0; i < fullBatches; i++) {
            String label = String.format("%s (Batch %d/%d - count: %d)",
                    stepName, i + 1, fullBatches + (remainder > 0 ? 1 : 0), maxBatchSize);
            String prompt = promptSupplier.apply(maxBatchSize);
            if (prompt != null) {
                sb.append(executor.execute(label, prompt)).append("\n\n\n");
            }
        }

        if (remainder > 0) {
            String label = String.format("%s (Remainder Batch - count: %d)", stepName, remainder);
            String prompt = promptSupplier.apply(remainder);
            if (prompt != null) {
                sb.append(executor.execute(label, prompt)).append("\n\n\n");
            }
        }

        return sb.toString();
    }
}