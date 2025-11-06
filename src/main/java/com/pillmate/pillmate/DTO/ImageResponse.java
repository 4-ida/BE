package com.pillmate.pillmate.DTO;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ImageResponse {
    private String drugId;
    private List<Image> images;
    private Meta meta;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Image {
        private String type; // front | back
        private String url;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Meta {
        private String color;
        private String shape;
        private String imprint;
    }
}
