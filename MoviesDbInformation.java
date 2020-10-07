package org.dima.movies;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MoviesDbInformation implements Serializable {
    /**
     * Тип коллекции
     */
    private String collection_type;
    private LocalDateTime init_time;
    private int elements_count;
    private Long max_id;

    public MoviesDbInformation(String collection_type, LocalDateTime init_time, int elements_count, Long max_id) {
        this.collection_type = collection_type;
        this.init_time = init_time;
        this.elements_count = elements_count;
        this.max_id = max_id;
    }

    public String getCollection_type() {
        return collection_type;
    }

    public LocalDateTime getInit_time() {
        return init_time;
    }

    public int getElements_count() {
        return elements_count;
    }

    public Long getMax_id() {
        return max_id;
    }
}
