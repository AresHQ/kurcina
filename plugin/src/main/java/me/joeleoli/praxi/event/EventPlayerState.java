package me.joeleoli.praxi.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventPlayerState {

    WAITING("Waiting"),
    ELIMINATED("Eliminated");

    private String readable;

}
