package com.pmo.userservice.infrastructure.filter;

import com.pmo.common.enums.PmoErrors;
import com.pmo.common.exception.ApplicationException;
import com.pmo.userservice.infrastructure.enums.FilterOperationEnum;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pmo.userservice.infrastructure.utils.Constants.COMMA;

/**
 * This class is used to extract any filter requested by the client.
 */
@Component
public class FilterBuilderService {


    private static List<String> split(String search, String delimiter) {
        return Stream.of(search.split(delimiter))
                .collect(Collectors.toList());
    }

    /**
     * Prepare filter condition, extract the different filters used in the controller via @RequestParam
     *
     * @param criteria            search Criteria.
     * @param filterOperationEnum filter operation
     * @return a list of {@link FilterCondition}
     */
    public List<FilterCondition> createFilterCondition(String criteria, FilterOperationEnum filterOperationEnum) {
        List<FilterCondition> filters = new ArrayList<>();

        try {
            if (criteria != null && !criteria.isEmpty()) {
                final String FILTER_SEARCH_DELIMITER = "&";
                final String FILTER_CONDITION_DELIMITER = "\\|";

                List<String> values = split(criteria, FILTER_SEARCH_DELIMITER);
                if (!values.isEmpty()) {
                    values.forEach(x -> {
                        List<String> filter = split(x, FILTER_CONDITION_DELIMITER);
                        FilterCondition filterCondition = FilterCondition.builder()
                                .field(filter.get(0))
                                .operator(filterOperationEnum)
                                .build();
                        String value = filter.get(1);
                        if (value.contains("[") && value.contains("]")) {
                            filterCondition.setOperator(FilterOperationEnum.IN);
                            value = value.replace("[", "").replace("]", "");
                            List<String> valueList = Arrays.stream(value.split(COMMA)).collect(Collectors.toList());
                            filterCondition.setValue(valueList);
                        } else {
                            filterCondition.setValue(value);
                        }
                        filters.add(filterCondition);
                    });
                }
            }

            return filters;

        } catch (Exception ex) {
            throw new ApplicationException(PmoErrors.BAD_REQUEST, criteria);
        }

    }

}
