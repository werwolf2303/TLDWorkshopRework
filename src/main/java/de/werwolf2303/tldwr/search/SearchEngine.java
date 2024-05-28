package de.werwolf2303.tldwr.search;

import de.werwolf2303.tldwr.workshop.WorkshopAPI;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class SearchEngine {
    private JComboBox<String> categories;

    public SearchEngine(JComboBox<String> categories) {
        this.categories = categories;
    }

    private class SearchEntry {
        public WorkshopAPI.Mod mod;
        public int relevance = 1;

        public SearchEntry(WorkshopAPI.Mod mod) {
            this.mod = mod;
        }
    }

    private void addToSearchEntry(WorkshopAPI.Mod mod, ArrayList<SearchEntry> entries) {
        boolean found = false;
        int entryNumber = 0;

        for (SearchEntry entry : entries) {
            if (entry.mod.Name.equals(mod.Name)) {
                found = true;
                break;
            }
            entryNumber++;
        }

        if (!found) {
            entries.add(new SearchEntry(mod));
        } else {
            entries.get(entryNumber).relevance++;
        }
    }

    public ArrayList<WorkshopAPI.Mod> search(ArrayList<WorkshopAPI.Mod> availableMods, String searchQuery) throws NullPointerException {
        ArrayList<WorkshopAPI.Mod> mods = new ArrayList<>();

        if (categories != null && !Objects.requireNonNull(categories.getSelectedItem()).toString().equals("All Categories")) {
            mods = searchCategory(availableMods, searchQuery);
        } else {
            mods = doSearch(searchQuery, availableMods);
        }

        return mods;
    }

    private ArrayList<WorkshopAPI.Mod> searchCategory(ArrayList<WorkshopAPI.Mod> availableMods, String searchQuery) {
        ArrayList<WorkshopAPI.Mod> mods = new ArrayList<>();

        for (WorkshopAPI.Mod mod : availableMods) {
            if (mod.Category.equals(categories.getSelectedItem())) {
                mods.add(mod);
            }
        }

        return doSearch(searchQuery, mods);
    }

    private String sanitizeSearchInput(String searchQuery) {
        return searchQuery.replaceAll("[^a-zA-Z0-9 ]", "");
    }

    private ArrayList<WorkshopAPI.Mod> doSearch(String searchQuery, ArrayList<WorkshopAPI.Mod> toSearch) {
        ArrayList<WorkshopAPI.Mod> mods = new ArrayList<>();
        ArrayList<SearchEntry> entries = new ArrayList<>();
        searchQuery = sanitizeSearchInput(searchQuery);
        searchQuery = searchQuery.toLowerCase();

        for (String searchTile : searchQuery.split(" ")) {
            for (WorkshopAPI.Mod mod : toSearch) {
                try {
                    if (mod.Name.toLowerCase().contains(searchTile)) {
                        addToSearchEntry(mod, entries);
                    }
                }catch (NullPointerException ignored) {
                }
                try {
                    if (mod.Author.toLowerCase().contains(searchTile)) {
                        addToSearchEntry(mod, entries);
                    }
                } catch (NullPointerException ignored) {
                }
                try {
                    if (mod.Category.toLowerCase().contains(searchTile)) {
                        addToSearchEntry(mod, entries);
                    }
                } catch (NullPointerException ignored) {
                }
                try {
                    if (mod.Description.toLowerCase().contains(searchTile)) {
                        addToSearchEntry(mod, entries);
                    }
                } catch (NullPointerException ignored) {
                }
            }
        }

        Collections.sort(entries, (o1, o2) -> {
            if (o1.relevance == o2.relevance)
                return 0;
            return o1.relevance > o2.relevance ? -1 : 1;
        });

        for(SearchEntry entry : entries) {
            mods.add(entry.mod);
        }

        return mods;
    }
}
