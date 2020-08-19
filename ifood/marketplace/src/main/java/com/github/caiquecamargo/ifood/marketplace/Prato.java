package com.github.caiquecamargo.ifood.marketplace;

import java.math.BigDecimal;
import java.util.stream.StreamSupport;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Tuple;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.pgclient.PgPool;

public class Prato {

  public Long id;

  public String nome;

  public String descricao;

  public Restaurante restaurante;

  public BigDecimal preco;

  private static Multi<PratoDTO> uniToMulti(Uni<RowSet<Row>> queryResult) {
    return queryResult.onItem().produceMulti(rowSet -> Multi.createFrom().items(() -> {
      return StreamSupport.stream(rowSet.spliterator(), false);
    })).onItem().apply(PratoDTO::from);
  }

  public static Multi<PratoDTO> findAll(PgPool pgPool) {
    Uni<RowSet<Row>> preparedQuery = pgPool.query("select * from prato").execute();
    return uniToMulti(preparedQuery);
  }

  public static Multi<PratoDTO> findAll(PgPool client, Long idRestaurante) {
    Uni<RowSet<Row>> preparedQuery = client
        .preparedQuery("SELECT * FROM prato where prato.restaurante_id = $1 ORDER BY nome ASC")
        .execute(Tuple.of(idRestaurante));
    return uniToMulti(preparedQuery);
  }
}