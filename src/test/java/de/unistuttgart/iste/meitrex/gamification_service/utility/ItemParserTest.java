package de.unistuttgart.iste.meitrex.gamification_service.utility;

import de.unistuttgart.iste.meitrex.gamification_service.model.ItemData;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ItemParserTest {

    private String filePath = "src/main/resources/itemSchema.json";

    @Test
    void testParse() throws IOException {
        ItemData items = ItemParser.parseFromFile(filePath);
        assertThat(items.getColorThemes(), hasSize(20));
    }
}
