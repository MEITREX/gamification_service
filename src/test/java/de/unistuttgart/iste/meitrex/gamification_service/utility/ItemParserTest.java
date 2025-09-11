package de.unistuttgart.iste.meitrex.gamification_service.utility;

import de.unistuttgart.iste.meitrex.gamification_service.model.ItemData;
import de.unistuttgart.iste.meitrex.gamification_service.model.access.DefaultItemProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ItemParserTest {

    private String filePath = "src/main/resources/itemSchema.json";

    DefaultItemProvider defaultItemProvider = new DefaultItemProvider(filePath);

    @Test
    void testParse() throws IOException {
        ItemData items = defaultItemProvider.load();
        assertThat(items.getColorThemes(), hasSize(20));
    }
}
