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
     * Traite les deux listes retourn�s par la m�thode
     * {@link #retrieveAddedAndRemoved(List, List, List, List)}.
     * Extrait les entit�s modifi�es et les place dans deux listes distinctes
     * (ancienne instance, nouvelle instance)
     * 
     * @param <E>
     *            le type de l'entit� utilis�e
     * @param addedList
     *            la liste des entit�s ajout�es (contient aussi les modifi�es)
     * @param deletedList
     *            la liste des entit�s supprim�es (contient aussi les modifi�es)
     * @param oldiesList
     *            la liste des anciennes instances d'objet des entit�s modifi�es
     * @return la liste des nouvelles instances d'objet des entit�s modifi�es
     */
    public static <E extends EntityBase> List<E> manageUpdated(
            List<E> addedList, List<E> deletedList, List<E> oldiesList) {
        
        // Initialise la liste stockant les nouvelles instances des entit�s
        // modifi�es
        List<E> updatedList = new ArrayList<E>();
        
        E added;
        E deleted;
        // Parcours en sens inverse pour permettre la suppression des objets
        for (int i = addedList.size() - 1; i >= 0; i--) {
            added = addedList.get(i);
            for (int j = deletedList.size() - 1; j >= 0; j--) {
                deleted = deletedList.get(j);
                
                // Si l'objet ajout� est aussi pr�sent dans la liste des objets
                // supprim�s
                if (added.getId() != null
                        && added.getId().equals(deleted.getId())) {
                    // Alors cet objet a �t� modifi� donc il faut
                    // l'ajouter � la liste des nouvelles instances
                    updatedList.add(added);
                    // le supprimer de la liste des ajouts
                    addedList.remove(added);
                    
                    // il faut �galement
                    // Ajouter l'ancienne instance � la liste des anciennes
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
