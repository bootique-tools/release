package io.bootique.tools.release.service.job;

import io.bootique.tools.release.model.github.Repository;
import io.bootique.tools.release.model.job.BatchJobResult;
import io.bootique.value.Percent;

import java.util.List;

public class JobResponse<T, R> {

    private Percent percent;
    private List<BatchJobResult<T, R>> results;
    private String name;

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {

        private JobResponse jobResponse;

        protected Builder() {
            this.jobResponse = new JobResponse();
        }

        public Builder percent(Percent percent) {
            this.jobResponse.percent = percent;
            return this;
        }

        public Builder results(List<BatchJobResult<Repository, String>> results) {
            this.jobResponse.results = results;
            return this;
        }

        public Builder name(String name) {
            this.jobResponse.name = name;
            return this;
        }

        public JobResponse build(){
            return jobResponse;
        }
    }

    public Percent getPercent() {
        return percent;
    }

    public List<BatchJobResult<T, R>> getResults() {
        return results;
    }

    public String getName() {
        return name;
    }
}
