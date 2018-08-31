package io.bootique.tools.release.service.job;

import io.bootique.tools.release.model.job.BatchJobResult;
import io.bootique.value.Percent;

import java.util.List;

public class JobRespnse<T, R> {

    private Percent percent;
    private List<BatchJobResult<T, R>> results;
    private String name;

    public JobRespnse(Percent percent, List<BatchJobResult<T, R>> results) {
        this.percent = percent;
        this.results = results;
    }

    public JobRespnse(Percent percent, List<BatchJobResult<T, R>> results, String name) {
        this.percent = percent;
        this.results = results;
        this.name = name;
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
