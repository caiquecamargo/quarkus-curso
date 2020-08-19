package com.github.caiquecamargo.ifood.cadastro;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Produces;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.github.caiquecamargo.ifood.cadastro.dto.AdicionarRestauranteDTO;
import com.github.caiquecamargo.ifood.cadastro.dto.RestauranteDTO;
import com.github.caiquecamargo.ifood.cadastro.dto.RestauranteMapper;
import com.github.caiquecamargo.ifood.cadastro.infra.ConstraintViolationResponse;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import org.eclipse.microprofile.reactive.messaging.Channel;

import org.eclipse.microprofile.reactive.messaging.Emitter;

@Path("/restaurantes")
@Tag(name = "restaurante")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("proprietario")
@SecurityScheme(securitySchemeName = "ifood-oauth", type = SecuritySchemeType.OAUTH2, flows = @OAuthFlows(password = @OAuthFlow(tokenUrl = "http://localhost:8180/auth/realms/ifood/protocol/openid-connect/token")))
public class ResturanteResource {

  @Inject
  RestauranteMapper restauranteMapper;

  @Inject
  @Channel("restaurantes")
  Emitter<String> emitter;

  @GET
  @Counted(name = "Quantidade de buscas em restaurantes")
  @SimplyTimed(name = "Tempo de busca em restaurantes")
  @Timed(name = "Tempo completo de busca")
  public List<RestauranteDTO> buscar() {
    Stream<Restaurante> restaurantes = Restaurante.streamAll();
    return restaurantes.map(r -> restauranteMapper.toRestauranteDTO(r)).collect(Collectors.toList());
  }

  @POST
  @Transactional
  @APIResponse(responseCode = "201", description = "Caso Restaurante seja cadastrado com sucesso")
  @APIResponse(responseCode = "400", content = @Content(schema = @Schema(allOf = ConstraintViolationResponse.class)))
  public Response adicionar(@Valid AdicionarRestauranteDTO dto) {
    Restaurante restaurante = restauranteMapper.toRestaurante(dto);
    restaurante.persist();

    Jsonb create = JsonbBuilder.create();
    String json = create.toJson(restaurante);

    emitter.send(json);

    return Response.status(Status.CREATED).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public void atualizar(@PathParam("id") Long id, Restaurante dto) {
    Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);

    if (restauranteOp.isEmpty()) {
      throw new NotFoundException();
    }

    Restaurante restaurante = restauranteOp.get();

    restaurante.nome = dto.nome;

    restaurante.persist();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public void delete(@PathParam("id") Long id) {
    Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);

    restauranteOp.ifPresentOrElse(Restaurante::delete, () -> {
      throw new NotFoundException();
    });
  }

  // PRATOS

  @GET
  @Tag(name = "prato")
  @Path("{idRestaurante}/pratos")
  public List<Prato> buscarPratos(@PathParam("idRestaurante") Long idRestaurante) {
    Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);

    if (restauranteOp.isEmpty())
      throw new NotFoundException("Restaurante não existe");

    return Prato.list("restaurante", restauranteOp.get());
  }

  @POST
  @Tag(name = "prato")
  @Path("{idRestaurante}/pratos")
  @Transactional
  public Response adicionarPrato(@PathParam("idRestaurante") Long idRestaurante, Prato dto) {
    Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);

    if (restauranteOp.isEmpty())
      throw new NotFoundException("Restaurante não existe");

    Prato prato = new Prato();
    prato.nome = dto.nome;
    prato.descricao = dto.descricao;
    prato.preco = dto.preco;
    prato.restaurante = restauranteOp.get();
    prato.persist();

    return Response.status(Status.CREATED).build();
  }

  @PUT
  @Tag(name = "prato")
  @Path("{idRestaurante}/pratos/{id}")
  @Transactional
  public void atualizarPrato(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id, Prato dto) {
    Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
    Optional<Prato> pratoOp = Prato.findByIdOptional(id);

    if (restauranteOp.isEmpty())
      throw new NotFoundException("Restaurante não existe");

    if (pratoOp.isEmpty())
      throw new NotFoundException("Prato não existe");

    Prato prato = pratoOp.get();
    prato.preco = dto.preco;

    prato.persist();
  }

  @DELETE
  @Tag(name = "prato")
  @Path("{idRestaurante}/pratos/{id}")
  @Transactional
  public void deletePrato(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id) {
    Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);
    Optional<Prato> pratoOp = Prato.findByIdOptional(id);

    if (restauranteOp.isEmpty())
      throw new NotFoundException("Restaurante não existe");

    pratoOp.ifPresentOrElse(Prato::delete, () -> {
      throw new NotFoundException();
    });
  }
}