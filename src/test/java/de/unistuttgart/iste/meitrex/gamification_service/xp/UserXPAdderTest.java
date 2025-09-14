package de.unistuttgart.iste.meitrex.gamification_service.xp;


import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.DefaultUserXPAdder;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserXPAdder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserXPAdderTest {

    public static DefaultUserXPAdder userXPAdder = new DefaultUserXPAdder();

    private static final UserEntity newUser = new UserEntity();

    private UserEntity createExperiencedUser(){
        final UserEntity experiencedUser = new UserEntity();
        experiencedUser.setXpValue(2000);
        return experiencedUser;
    }

    private UserEntity createNewUser() {
        final UserEntity experiencedUser = new UserEntity();
        experiencedUser.setXpValue(null);
        return experiencedUser;
    }


    @Test
    public void testAddContractByValue() {
        assertThrows(NullPointerException.class, () -> userXPAdder.add(null, 0));
        assertThrows(IllegalArgumentException.class, () -> userXPAdder.add(createExperiencedUser(), -10));
        assertDoesNotThrow(() -> userXPAdder.add(createExperiencedUser(), 1000));
    }

    @Test
    public void testAddContractByCause() {
        assertThrows(NullPointerException.class, () -> userXPAdder.add(createExperiencedUser(), null));
        assertDoesNotThrow(() -> userXPAdder.add(createExperiencedUser(), IUserXPAdder.Cause.COURSE_COMPLETED));
    }

    @Test
    public void testAddContractByCauseAndMultiple() {
        assertThrows(NullPointerException.class, () -> userXPAdder.add(createExperiencedUser(), null, 10));
        assertThrows(IllegalArgumentException.class, () -> userXPAdder.add(createExperiencedUser(), IUserXPAdder.Cause.COURSE_COMPLETED, -10));
        assertDoesNotThrow(() -> userXPAdder.add(createExperiencedUser(), IUserXPAdder.Cause.VIDEO_WATCHED, 100));

    }

    @Test
    public void testAddByValueForNewUser() {
        final UserEntity newUser = createNewUser();
        userXPAdder.add(newUser, 100);
        assertEquals(100, newUser.getXpValue());
    }

    @Test
    public void testAddByValueForExperiencedUser() {
        final UserEntity experiencedUser = createExperiencedUser();
        userXPAdder.add(experiencedUser, 100);
        assertEquals(2100, experiencedUser.getXpValue());
    }

    @Test
    public void testAddByCauseForNewUser() {
        final UserEntity newUser = createNewUser();
        userXPAdder.add(newUser, IUserXPAdder.Cause.COURSE_COMPLETED);
        assertEquals(500, newUser.getXpValue());
    }

    @Test
    public void testAddByCauseForExperiencedUser() {
        final UserEntity experiencedUser = createExperiencedUser();
        userXPAdder.add(experiencedUser, IUserXPAdder.Cause.COURSE_COMPLETED);
        assertEquals(2500, experiencedUser.getXpValue());
    }

    @Test
    public void testAddByCauseAndMultipleForNewUser() {
        final UserEntity newUser = createNewUser();
        userXPAdder.add(newUser, IUserXPAdder.Cause.VIDEO_WATCHED, 10);
        assertEquals(20, newUser.getXpValue());
    }

    @Test
    public void testAddByCauseAndMultipleForExperiencedUser() {
        final UserEntity experiencedUser = createExperiencedUser();
        userXPAdder.add(experiencedUser, IUserXPAdder.Cause.VIDEO_WATCHED, 10);
        assertEquals(2020, experiencedUser.getXpValue());
    }

}
