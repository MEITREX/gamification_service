package de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class MappingUtility {

    private MappingUtility() {}

    static String toString(Object obj) {
        if(Objects.isNull(obj)) {
            return null;
        }
        return obj.toString();
    }

    static <T> List<T> nullToEmptyList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }



}
