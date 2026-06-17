package ee.joeltek.match_me.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DisplayNameGeneratorTest {

    @Mock
    private UserProfileRepository profileRepository;

    @Test
    void generateUniqueDisplayNameReturnsNonBlankName() {
        //arrange
        DisplayNameGenerator generator = new DisplayNameGenerator(profileRepository);
        String displayName;
        //when(...)
        when(profileRepository.existsByDisplayNameIgnoreCase(anyString())).thenReturn(false);
        //act
        displayName = generator.generateUniqueDisplayName();
        //assert
        assertThat(displayName).isNotNull();
        assertThat(displayName).isNotEmpty();
        assertThat(displayName).isNotBlank();
    }

    @Test
    void generateUniqueDisplayNameChecksRepositoryForUniqueness() {
        DisplayNameGenerator generator = new DisplayNameGenerator(profileRepository);

        when(profileRepository.existsByDisplayNameIgnoreCase(anyString())).thenReturn(false);

        generator.generateUniqueDisplayName();

        verify(profileRepository, atLeastOnce()).existsByDisplayNameIgnoreCase(anyString());

    }

    @Test
    void generateUniqueDisplayNameRetriesWhenNameAlreadyExists(){
        DisplayNameGenerator generator = new DisplayNameGenerator(profileRepository);
        String displayName;

        when(profileRepository.existsByDisplayNameIgnoreCase(anyString())).thenReturn(true, false);

        displayName = generator.generateUniqueDisplayName();

        verify(profileRepository, times(2)).existsByDisplayNameIgnoreCase(anyString());
        assertThat(displayName).isNotNull();
        assertThat(displayName).isNotEmpty();
        assertThat(displayName).isNotBlank();
    }

}
