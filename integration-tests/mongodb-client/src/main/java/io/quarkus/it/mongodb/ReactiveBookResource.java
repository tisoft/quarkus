package io.quarkus.it.mongodb;

import static com.mongodb.client.model.Filters.eq;

import java.util.List;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;

@Path("/reactive-books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReactiveBookResource {

    @Inject
    ReactiveMongoClient client;

    private ReactiveMongoCollection<Book> collection;

    @PostConstruct
    public void init() {
        collection = client.getDatabase("books").getCollection("my-reactive-collection", Book.class);
    }

    @GET
    public CompletionStage<List<Book>> getBooks() {
        return collection.find().collectItems().asList().subscribeAsCompletionStage();
    }

    @POST
    public CompletionStage<Response> addBook(Book book) {
        return collection.insertOne(book)
                .onItem().apply(x -> Response.accepted().build())
                .subscribeAsCompletionStage();
    }

    @GET
    @Path("/{author}")
    public CompletionStage<List<Book>> getBooksByAuthor(@PathParam("author") String author) {
        return collection.find(eq("author", author))
                .collectItems().asList()
                .subscribeAsCompletionStage();
    }

}
