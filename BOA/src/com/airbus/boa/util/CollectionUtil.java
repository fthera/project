/*
 * ------------------------------------------------------------------------
 * Class : CollectionUtil
 * Copyright 2011 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.util;

import java.util.ArrayList;
import java.util.List;

import com.airbus.boa.entity.EntityBase;

/**
 * @author ng0057cf
 */
public class CollectionUtil {
    
    private CollectionUtil() {
    }
    
    public static <T> void retrieveAddedAndRemoved(final List<T> oldList,
            final List<T> newList, List<T> added, List<T> removed) {
        if (!(added.isEmpty() || removed.isEmpty())) {
            throw new IllegalArgumentException("added or removed must be empty");
        }
        removed.addAll(difference(oldList, newList));
        added.addAll(difference(newList, oldList));
    }
    
    private static <T> List<T> difference(List<T> listA, List<T> listB) {
        
        List<T> result = new ArrayList<T>();
        if (listA != null) {
            result.addAll(listA);
        }
        if (listB != null) {
            result.removeAll(listB);
        }
        return result;
    }
    
    /**
     * Traite les deux listes retournés par la méthode
     * {@link #retrieveAddedAndRemoved(List, List, List, List)}.
     * Extrait les entités modifiées et les place dans deux listes distinctes
     * (ancienne instance, nouvelle instance)
     * 
     * @param <E>
     *            le type de l'entité utilisée
     * @param addedList
     *            la liste des entités ajoutées (contient aussi les modifiées)
     * @param deletedList
     *            la liste des entités supprimées (contient aussi les modifiées)
     * @param oldiesList
     *            la liste des anciennes instances d'objet des entités modifiées
     * @return la liste des nouvelles instances d'objet des entités modifiées
     */
    public static <E extends EntityBase> List<E> manageUpdated(
            List<E> addedList, List<E> deletedList, List<E> oldiesList) {
        
        // Initialise la liste stockant les nouvelles instances des entités
        // modifiées
        List<E> updatedList = new ArrayList<E>();
        
        E added;
        E deleted;
        // Parcours en sens inverse pour permettre la suppression des objets
        for (int i = addedList.size() - 1; i >= 0; i--) {
            added = addedList.get(i);
            for (int j = deletedList.size() - 1; j >= 0; j--) {
                deleted = deletedList.get(j);
                
                // Si l'objet ajouté est aussi présent dans la liste des objets
                // supprimés
                if (added.getId() != null
                        && added.getId().equals(deleted.getId())) {
                    // Alors cet objet a été modifié donc il faut
                    // l'ajouter à la liste des nouvelles instances
                    updatedList.add(added);
                    // le supprimer de la liste des ajouts
                    addedList.remove(added);
                    
                    // il faut également
                    // Ajouter l'ancienne instance à la liste des anciennes
                    // instance
                    oldiesList.add(deleted);
                    // et le supprimer de la liste des suppressions
                    deletedList.remove(deleted);
                }
            }
        }
        return updatedList;
    }
    
}
