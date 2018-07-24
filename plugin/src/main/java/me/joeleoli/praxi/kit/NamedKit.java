package me.joeleoli.praxi.kit;

import lombok.AllArgsConstructor;
import lombok.Data;

import me.joeleoli.commons.util.Pair;
import me.joeleoli.praxi.script.Replaceable;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Data
public class NamedKit extends Kit implements Replaceable {

    private String name;

    @Override
    public List<Pair<String, String>> getReplacements() {
        return Collections.singletonList(new Pair<>("kit_name", this.name));
    }

}
