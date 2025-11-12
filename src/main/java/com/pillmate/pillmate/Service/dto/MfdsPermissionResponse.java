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
public class MfdsPermissionResponse {

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
        private List<PermissionItem> items;

        public List<PermissionItem> getItems() {
            return items == null ? Collections.emptyList() : items;
        }
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PermissionItem {
        @JsonProperty("PRDLST_STDR_CODE")
        private String prdlstStdrCodeUpper;

        @JsonProperty("prdlstStdrCode")
        private String prdlstStdrCode;

        @JsonProperty("ITEM_NAME")
        private String itemNameUpper;

        @JsonProperty("itemName")
        private String itemName;

        @JsonProperty("SPCLTY_PBLC")
        private String spcltyPblcUpper;

        @JsonProperty("spcltyPblc")
        private String spcltyPblc;

        @JsonProperty("ETC_OTC_CODE")
        private String etcOtcCodeUpper;

        @JsonProperty("etcOtcCode")
        private String etcOtcCode;

        @JsonProperty("ITEM_INGR_NAME")
        private String itemIngrNameUpper;

        @JsonProperty("itemIngrName")
        private String itemIngrName;

        @JsonProperty("ATC_CODE")
        private String atcCodeUpper;

        @JsonProperty("atcCode")
        private String atcCode;

        @JsonProperty("STORAGE_METHOD")
        private String storageMethodUpper;

        @JsonProperty("storageMethod")
        private String storageMethod;

        @JsonProperty("PERMIT_DATE")
        private String permitDateUpper;

        @JsonProperty("permitDate")
        private String permitDate;

        @JsonProperty("APPRNC")
        private String appearanceUpper;

        @JsonProperty("apprnc")
        private String appearance;

        public String getResolvedItemSeq() {
            return firstNonBlank(prdlstStdrCode, prdlstStdrCodeUpper);
        }

        public String getResolvedItemName() {
            return firstNonBlank(itemName, itemNameUpper);
        }

        public String getResolvedSpcltyPblc() {
            return firstNonBlank(spcltyPblc, spcltyPblcUpper);
        }

        public String getResolvedEtcOtcCode() {
            return firstNonBlank(etcOtcCode, etcOtcCodeUpper);
        }

        public String getResolvedMainIngredient() {
            return firstNonBlank(itemIngrName, itemIngrNameUpper);
        }

        public String getResolvedAtcCode() {
            return firstNonBlank(atcCode, atcCodeUpper);
        }

        public String getResolvedStorageMethod() {
            return firstNonBlank(storageMethod, storageMethodUpper);
        }

        public String getResolvedPermitDate() {
            return firstNonBlank(permitDate, permitDateUpper);
        }

        public String getResolvedAppearance() {
            return firstNonBlank(appearance, appearanceUpper);
        }

        private String firstNonBlank(String... values) {
            if (values == null) return null;
            for (String value : values) {
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
            return null;
        }
    }
}



