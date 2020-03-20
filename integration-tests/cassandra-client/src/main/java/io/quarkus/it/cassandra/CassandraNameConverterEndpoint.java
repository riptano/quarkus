package io.quarkus.it.cassandra;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.it.cassandra.dao.nameconverters.NameConverterEntity;

@Path("/cassandra-name-converter")
public class CassandraNameConverterEndpoint {
    @Inject
    private NameConvertedDaoService dao;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/product/{id}")
    public Integer saveProduct(@PathParam("id") Integer id) {
        dao.getDao().save(new NameConverterEntity(id));
        return id;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/product/{id}")
    public NameConverterEntity getProduct(@PathParam("id") Integer id) {
        return dao.getDao().findById(id);
    }

}
