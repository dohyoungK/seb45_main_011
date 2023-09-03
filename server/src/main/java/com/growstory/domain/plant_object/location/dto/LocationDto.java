package com.growstory.domain.plant_object.location.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class LocationDto {

    @Getter
    public static class Post {
        private int x;
        private int y;
    }

    @Getter
    @Builder
    public static class Patch {
        private Long locationId;
        private int x;
        private int y;
        boolean isInstalled;
    }

    //TODO: GardenInfo에도 Response를 줘야 한다.
    @Getter
    @Builder
    public static class Response {
        private Long locationId;
        private int x;
        private int y;
        boolean isInstalled;
    }
}