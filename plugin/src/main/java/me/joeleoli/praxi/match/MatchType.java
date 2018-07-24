package me.joeleoli.praxi.match;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MatchType {

    ONE_VS_ONE("1vs1"),
    TWO_VS_TWO("2vs2"),
    TEAM_SPLIT("Team Split"),
    TEAM_FFA("Team FFA");

    private String name;

}
