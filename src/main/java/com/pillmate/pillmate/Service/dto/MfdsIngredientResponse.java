package com.pillmate.pillmate.Service.dto;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MfdsIngredientResponse {

    @JsonProperty("header")
    private Header header;

    @JsonProperty("body")
    private Body body;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("totalCount")
        private Integer totalCount;

        @JsonProperty("items")
        private List<Item> items;

        public List<Item> getItems() {
            return items == null ? Collections.emptyList() : items;
        }
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JsonProperty("itemSeq")
        private String itemSeq;

        @JsonProperty("ingrCode")
        private String ingredientCode;

        @JsonProperty("ingrName")
        private String ingredientName;

        @JsonProperty("ingrEngName")
        private String ingredientEngName;

        @JsonProperty("mainIngr")
        private String mainIngredient; // "Y" or "N"

        @JsonProperty("unit")
        private String unit;

        @JsonProperty("content")
        private String content;
    }
}



