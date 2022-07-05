package io.bootique.tools.release.service.job;

import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.model.job.BatchJobResult;
import io.bootique.tools.release.model.job.BatchJobStatus;
import io.bootique.tools.release.service.preferences.MockPreferenceService;
import io.bootique.value.Percent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class DefaultBatchJobServiceTest {

    private DefaultBatchJobService batchJobService;

    @BeforeEach
    void createService() {
        batchJobService = new DefaultBatchJobService(new MockPreferenceService());
    }

    @Test
    void testSubmit() throws Exception {
        CountDownLatch latch = new CountDownLatch(4);

        BatchJobDescriptor<String, Void> descriptor = BatchJobDescriptor.<String, Void>builder()
                .data(Arrays.asList("str1", "str2", "str3", "str4"))
                .processor(str -> {
                    try {
                        return null;
                    } finally {
                        latch.countDown();
                    }
                }).build();

        BatchJob<String, Void> job = batchJobService.submit(descriptor);

        latch.await();
        // wait for result to propagate back to this thread
        Thread.sleep(10);

        assertNotNull(job);
        assertEquals(0L, job.getId());
        assertEquals(4, job.getTotal());
        assertEquals(Percent.HUNDRED, job.getProgress());

        List<BatchJobResult<String, Void>> resultList = job.getResults();
        assertEquals(4, resultList.size());
        resultList.forEach(r -> {
            assertTrue(r.data().contains("str"));
            assertEquals(BatchJobStatus.SUCCESS, r.status());
        });
    }
}