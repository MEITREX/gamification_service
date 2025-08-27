package de.unistuttgart.iste.meitrex.gamification_service.model.access;


import de.unistuttgart.iste.meitrex.gamification_service.model.ItemData;

import java.io.*;

public interface IItemProvider {
    ItemData load()
            throws IOException;
}
