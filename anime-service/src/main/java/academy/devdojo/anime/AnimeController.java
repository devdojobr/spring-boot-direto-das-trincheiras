package academy.devdojo.anime;

import academy.devdojo.api.AnimeControllerApi;
import academy.devdojo.dto.AnimeGetResponse;
import academy.devdojo.dto.AnimePostRequest;
import academy.devdojo.dto.AnimePostResponse;
import academy.devdojo.dto.AnimePutRequest;
import academy.devdojo.dto.PageAnimeGetResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/animes")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "basicAuth")
public class AnimeController implements AnimeControllerApi {

  private final AnimeMapper mapper;
  private final AnimeService service;

  @GetMapping
  public ResponseEntity<List<AnimeGetResponse>> findAllAnimes(@RequestParam(required = false) String name) {
    log.debug("Request received to list all animes, param name '{}'", name);

    var animes = service.findAll(name);

    var animeGetResponses = mapper.toAnimeGetResponseList(animes);

    return ResponseEntity.ok(animeGetResponses);
  }

  @Override
  @GetMapping("/paginated")
  public ResponseEntity<PageAnimeGetResponse> findAllAnimesPaginated(
      @PageableDefault(page = 0, size = 20) @ParameterObject final Pageable pageable
  ) {
    log.debug("Request received to list all animes paginated");

    var jpaPageAnimeGetResponse = service.findAllPaginated(pageable);
    var pageAnimeGetResponse = mapper.toPageAnimeGetResponse(jpaPageAnimeGetResponse);

    return ResponseEntity.ok(pageAnimeGetResponse);
  }

  @GetMapping("{id}")
  public ResponseEntity<AnimeGetResponse> findByAnimeId(@PathVariable Long id) {
    log.debug("Request to find anime by id: {}", id);

    var anime = service.findByIdOrThrowNotFound(id);

    var animeGetResponse = mapper.toAnimeGetResponse(anime);

    return ResponseEntity.ok(animeGetResponse);
  }

  @PostMapping
  public ResponseEntity<AnimePostResponse> saveAnime(@RequestBody @Valid AnimePostRequest request) {
    log.debug("Request to save anime : {}", request);
    var anime = mapper.toAnime(request);

    var animeSaved = service.save(anime);

    var animeGetResponse = mapper.toAnimePostResponse(animeSaved);

    return ResponseEntity.status(HttpStatus.CREATED).body(animeGetResponse);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<Void> deleteAnimeById(@PathVariable Long id) {
    log.debug("Request to delete anime by id: {}", id);

    service.delete(id);

    return ResponseEntity.noContent().build();
  }

  @PutMapping
  public ResponseEntity<Void> updateAnime(@RequestBody @Valid AnimePutRequest request) {
    log.debug("Request to update anime {}", request);

    var animeToUpdate = mapper.toAnime(request);

    service.update(animeToUpdate);

    return ResponseEntity.noContent().build();
  }

}
