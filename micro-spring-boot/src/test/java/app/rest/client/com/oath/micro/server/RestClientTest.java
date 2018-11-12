
package app.rest.client.com.oath.micro.server;


import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import app.boot.front.end.BootExample;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;

import com.oath.micro.server.MicroserverApp;
import com.oath.micro.server.config.Microserver;
import com.oath.micro.server.rest.client.nio.AsyncRestClient;
import com.oath.micro.server.rest.client.nio.NIORestClient;
import com.oath.micro.server.rest.client.nio.SpringConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.oath.micro.server.spring.boot.MicroSpringBoot;

@Microserver @MicroSpringBoot
public class RestClientTest {

   	private final AsyncRestClient<List<String>> listClient = new AsyncRestClient(1000,1000).withResponse(List.class);
	private final AsyncRestClient<ImmutableSet<String>> setClient = new AsyncRestClient(1000,1000).withResponse(ImmutableSet.class);;
	private final AsyncRestClient<ImmutableList<MyEntity>> genericsClient = new AsyncRestClient(1000,1000).withGenericResponse(ImmutableList.class, MyEntity.class);
  
	private final NIORestClient rest = new SpringConfig().restClient();
																			

	MicroserverApp server;
	static volatile boolean run = false;
	@Before
	public void startServer(){
		if(run)
			return;
		run = true;
		Class<?>[] classes = new MicroserverApp(true, RestClientTest.class, ()-> "rest-app").classes;
		for(Class n : classes){
		    System.out.println("Class " + n);
        }
        //SpringApplication.run(classes,new String[0]);
		server = new MicroserverApp( RestClientTest.class, ()-> "rest-app");
		System.out.println("paused");

	}
	
	
	/*
	 * Simpler with JaxRsNIOClient
	 */
	@Test
	public void testCRUD() throws InterruptedException, ExecutionException{
		
		
		assertThat(listClient.get("http://localhost:8080/rest-app/rest/get").get().get(0),is("ok"));
		assertThat(setClient.post("http://localhost:8080/rest-app/rest/post",ImmutableMap.of(1,"hello")).get(),is(ImmutableSet.of("hello")));
		assertThat(setClient.put("http://localhost:8080/rest-app/rest/put",ImmutableMap.of(1,"hello")).get(),is(ImmutableSet.of("hello")));
		assertThat(listClient.delete("http://localhost:8080/rest-app/rest/delete").get().get(0),is("ok"));
	}
	
	@Test
	public void testCRUDGenerics() throws InterruptedException, ExecutionException{
		
		
		assertThat(genericsClient.get("http://localhost:8080/rest-app/generics/get").get().get(0),is(new MyEntity()));
		assertThat(genericsClient.post("http://localhost:8080/rest-app/generics/post",ImmutableMap.of(1,"hello")).get(),is(ImmutableList.of(new MyEntity())));
		assertThat(genericsClient.put("http://localhost:8080/rest-app/generics/put",ImmutableMap.of(1,"hello")).get(),is(ImmutableList.of(new MyEntity())));
		assertThat(genericsClient.delete("http://localhost:8080/rest-app/generics/delete").get().get(0),is(new MyEntity()));
	}
	
	/**
	 * More complex with Spring REST Template Based NIORestTemplate
	 *
	 */
	
	@Test
	public void testCRUDSpring() throws InterruptedException, ExecutionException, RestClientException, URISyntaxException{
		
		
		assertThat(rest.getForEntity(new URI("http://localhost:8080/rest-app/rest/get"),List.class).get().getBody().get(0),is("ok"));
		
		assertThat(rest.postForEntity("http://localhost:8080/rest-app/rest/post", new HttpEntity(ImmutableMap.of(1,"hello")), ImmutableSet.class).get().getBody(),is(ImmutableSet.of("hello")));
		assertThat( rest.put("http://localhost:8080/rest-app/rest/put",new HttpEntity(ImmutableMap.of(1,"hello")),ImmutableSet.class).get()
									,is(nullValue()));
		assertThat(rest.delete("http://localhost:8080/rest-app/rest/delete").get(),is(nullValue()));
	}
	
	@Test
	public void testCRUDGenericsSpring() throws InterruptedException, ExecutionException{
		
		
		assertThat(rest.exchange("http://localhost:8080/rest-app/generics/get",HttpMethod.GET,null,new ParameterizedTypeReference<ImmutableList<MyEntity>>(){})
				.get().getBody().get(0),is(new MyEntity()));
		
		assertThat(rest.exchange("http://localhost:8080/rest-app/generics/post",HttpMethod.POST,new HttpEntity(ImmutableMap.of(1,"hello")),new ParameterizedTypeReference<ImmutableList<MyEntity>>(){})
				.get().getBody(),is(ImmutableList.of(new MyEntity())));
		
		assertThat(rest.exchange("http://localhost:8080/rest-app/generics/put",HttpMethod.PUT,new HttpEntity(ImmutableMap.of(1,"hello")),new ParameterizedTypeReference<ImmutableList<MyEntity>>(){})
				.get().getBody(),is(ImmutableList.of(new MyEntity())));
		
		assertThat(rest.exchange("http://localhost:8080/rest-app/generics/delete",HttpMethod.DELETE,null,new ParameterizedTypeReference<ImmutableList<MyEntity>>(){})
				.get().getBody().get(0),is(new MyEntity()));
		
	}
	
	
	
}
