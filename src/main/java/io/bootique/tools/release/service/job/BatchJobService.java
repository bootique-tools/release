package io.bootique.tools.release.service.job;

import io.bootique.tools.release.model.job.BatchJob;
import io.bootique.tools.release.model.job.BatchJobDescriptor;
import io.bootique.tools.release.service.preferences.Preference;

public interface BatchJobService {

    Preference<Long> CURRENT_JOB_ID = Preference.of("current.job.id", Long.class);

    <T, R> BatchJob<T, R> submit(BatchJobDescriptor<T, R> descriptor);

    <T, R> BatchJob<T, R> getJobById(long id);

    <T, R> BatchJob<T, R> getCurrentJob();
}
