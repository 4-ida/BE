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
public class MfdsEasyDrugResponse {

    @JsonProperty("header")
    private MfdsEasyDrugHeader header;

    @JsonProperty("body")
    private MfdsEasyDrugBody body;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MfdsEasyDrugHeader {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MfdsEasyDrugBody {
        @JsonProperty("totalCount")
        private Integer totalCount;

        @JsonProperty("items")
        private List<MfdsEasyDrugItem> items;

        public List<MfdsEasyDrugItem> getItems() {
            return items == null ? Collections.emptyList() : items;
        }
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MfdsEasyDrugItem {
        @JsonProperty("itemSeq")
        private String itemSeq;

        @JsonProperty("itemName")
        private String itemName;

        @JsonProperty("entpName")
        private String entpName;

        @JsonProperty("etcOtcName")
        private String etcOtcName;

        @JsonProperty("chart")
        private String chart;

        @JsonProperty("itemImage")
        private String itemImage;

        @JsonProperty("efcyQesitm")
        private String efcyQesitm;

        @JsonProperty("useMethodQesitm")
        private String useMethodQesitm;

        @JsonProperty("atpnWarnQesitm")
        private String atpnWarnQesitm;

        @JsonProperty("atpnQesitm")
        private String atpnQesitm;

        @JsonProperty("intrcQesitm")
        private String intrcQesitm;

        @JsonProperty("seQesitm")
        private String seQesitm;

        @JsonProperty("depositMethodQesitm")
        private String depositMethodQesitm;

        @JsonProperty("openDe")
        private String openDe;

        @JsonProperty("updateDe")
        private String updateDe;
    }
}



