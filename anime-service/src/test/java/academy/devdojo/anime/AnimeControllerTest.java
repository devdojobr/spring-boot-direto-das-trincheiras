package academy.devdojo.anime;

import academy.devdojo.commons.AnimeUtils;
import academy.devdojo.commons.FileUtils;
import academy.devdojo.domain.Anime;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@WebMvcTest(controllers = AnimeController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ComponentScan(basePackages = {"academy.devdojo.anime", "academy.devdojo.commons", "academy.devdojo.config"})
@WithMockUser
class AnimeControllerTest {
    private static final String URL = "/v1/animes";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AnimeRepository repository;
    private List<Anime> animesList;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private AnimeUtils animeUtils;

    @BeforeEach
    void init() {
        animesList = animeUtils.newAnimeList();
    }

    @Test
    @DisplayName("GET v1/animes returns a list with all animes when argument is null")
    @Order(1)
    void findAll_ReturnsAllAnimes_WhenArgumentIsNull() throws Exception {
        var response = fileUtils.readResourceFile("anime/get-anime-null-name-200.json");
        when(repository.findAll()).thenReturn(animesList);

        mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Test
    @DisplayName("GET v1/animes returns 403 when role is not User")
    @Order(1)
    @WithMockUser(roles = "ADMIN")
    void findAll_Returns403_WhenRoleIsNotUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("GET v1/animes/paginated returns a paginated list of animes")
    @Order(1)
    void findAllPaginated_ReturnsPaginatedAnimes_WhenSuccessful() throws Exception {
        var response = fileUtils.readResourceFile("anime/get-anime-paginated-200.json");
        var pageRequest = PageRequest.of(0, animesList.size());
        var pageAnime = new PageImpl<Anime>(animesList, pageRequest, 1);

        when(repository.findAll(any(Pageable.class))).thenReturn(pageAnime);

        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/paginated"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Test
    @DisplayName("GET v1/animes?name=Mashle returns list with found object when name exists")
    @Order(2)
    void findAll_ReturnsFoundAnimeList_WhenNameIsFound() throws Exception {
        var response = fileUtils.readResourceFile("anime/get-anime-mashle-name-200.json");
        var name = "Mashle";
        var mashle = animesList.stream().filter(anime -> anime.getName().equals(name)).findFirst().orElse(null);

        when(repository.findByName(name)).thenReturn(Collections.singletonList(mashle));

        mockMvc.perform(MockMvcRequestBuilders.get(URL).param("name", name))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Test
    @DisplayName("GET v1/animes?name=x returns empty list when name is not found")
    @Order(3)
    void findAll_ReturnsEmptyList_WhenNameIsNotFound() throws Exception {
        var response = fileUtils.readResourceFile("anime/get-anime-x-name-200.json");
        var name = "x";

        mockMvc.perform(MockMvcRequestBuilders.get(URL).param("name", name))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Test
    @DisplayName("GET v1/animes/1 returns an anime with given id")
    @Order(4)
    void findById_ReturnsAnimeById_WhenSuccessful() throws Exception {
        var response = fileUtils.readResourceFile("anime/get-anime-by-id-200.json");
        var id = 1L;
        var foundAnime = animesList.stream().filter(anime -> anime.getId().equals(id)).findFirst();
        when(repository.findById(id)).thenReturn(foundAnime);

        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Test
    @DisplayName("GET v1/animes/99 throws NotFound 404 when anime is not found")
    @Order(5)
    void findById_ThrowsNotFound_WhenAnimeIsNotFound() throws Exception {
        var response = fileUtils.readResourceFile("anime/get-anime-by-id-404.json");

        var id = 99L;

        mockMvc.perform(MockMvcRequestBuilders.get(URL + "/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Test
    @DisplayName("POST v1/animes creates an anime")
    @Order(6)
    void save_CreatesAnime_WhenSuccessful() throws Exception {
        var request = fileUtils.readResourceFile("anime/post-request-anime-200.json");
        var response = fileUtils.readResourceFile("anime/post-response-anime-201.json");
        var animeToSave = animeUtils.newAnimeToSave();

        when(repository.save(ArgumentMatchers.any())).thenReturn(animeToSave);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(response));
    }

    @Test
    @DisplayName("DELETE v1/animes/1 removes an anime")
    @Order(7)
    void delete_RemoveAnime_WhenSuccessful() throws Exception {
        var id = animesList.getFirst().getId();
        var foundAnime = animesList.stream().filter(anime -> anime.getId().equals(id)).findFirst();
        when(repository.findById(id)).thenReturn(foundAnime);

        mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("DELETE v1/animes/99 throws NotFound when anime is not found")
    @Order(8)
    void delete_ThrowsNotFound_WhenAnimeIsNotFound() throws Exception {
        var response = fileUtils.readResourceFile("anime/delete-anime-by-id-404.json");

        var id = 99L;

        mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/{id}", id))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().json(response));

    }

    @Test
    @DisplayName("PUT v1/animes updates an anime")
    @Order(9)
    void update_UpdatesAnime_WhenSuccessful() throws Exception {
        var request = fileUtils.readResourceFile("anime/put-request-anime-200.json");
        var id = 1L;
        var foundAnime = animesList.stream().filter(anime -> anime.getId().equals(id)).findFirst();
        when(repository.findById(id)).thenReturn(foundAnime);

        mockMvc.perform(MockMvcRequestBuilders
                        .put(URL)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("PUT v1/animes throws NotFound when anime is not found")
    @Order(10)
    void update_ThrowsNotFound_WhenAnimeIsNotFound() throws Exception {
        var request = fileUtils.readResourceFile("anime/put-request-anime-404.json");
        var response = fileUtils.readResourceFile("anime/put-anime-by-id-404.json");

        mockMvc.perform(MockMvcRequestBuilders
                        .put(URL)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().json(response));

    }

    @ParameterizedTest
    @MethodSource("postAnimeBadRequestSource")
    @DisplayName("POST v1/animes returns bad request when fields are invalid")
    @Order(11)
    void save_ReturnsBadRequest_WhenFieldsAreInvalid(String fileName, List<String> errors) throws Exception {
        var request = fileUtils.readResourceFile("anime/%s".formatted(fileName));

        var mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post(URL)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        var resolvedException = mvcResult.getResolvedException();

        assertThat(resolvedException).isNotNull();

        assertThat(resolvedException.getMessage()).contains(errors);
    }

    @ParameterizedTest
    @MethodSource("putAnimeBadRequestSource")
    @DisplayName("PUT v1/animes returns bad request when fields are invalid")
    @Order(12)
    void update_ReturnsBadRequest_WhenFieldsAreInvalid(String fileName, List<String> errors) throws Exception {
        var request = fileUtils.readResourceFile("anime/%s".formatted(fileName));

        var mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .put(URL)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        var resolvedException = mvcResult.getResolvedException();

        assertThat(resolvedException).isNotNull();

        assertThat(resolvedException.getMessage()).contains(errors);
    }

    private static Stream<Arguments> putAnimeBadRequestSource() {
        var allRequiredErrors = allRequiredErrors();
        allRequiredErrors.add("The field 'id' cannot be null");

        return Stream.of(
                Arguments.of("put-request-anime-empty-fields-400.json", allRequiredErrors),
                Arguments.of("put-request-anime-blank-fields-400.json", allRequiredErrors)
        );
    }

    private static Stream<Arguments> postAnimeBadRequestSource() {
        var allRequiredErrors = allRequiredErrors();

        return Stream.of(
                Arguments.of("post-request-anime-empty-fields-400.json", allRequiredErrors),
                Arguments.of("post-request-anime-blank-fields-400.json", allRequiredErrors)
        );
    }

    private static List<String> allRequiredErrors() {
        var nameRequiredError = "The field 'name' is required";

        return new ArrayList<>(List.of(nameRequiredError));
    }


}