package com.mortimer.heart;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WikiDpsResolverTest
{
	@Test
	public void usesFixedWikiEndpointAndNeverFollowsRedirects()
	{
		AtomicInteger requests = new AtomicInteger();
		AtomicReference<HttpUrl> requestedUrl = new AtomicReference<>();
		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain ->
		{
			requests.incrementAndGet();
			requestedUrl.set(chain.request().url());
			return new Response.Builder()
				.request(chain.request())
				.protocol(Protocol.HTTP_1_1)
				.code(302)
				.message("Found")
				.header("Location", "https://unreviewed.example/response-provided-target")
				.body(ResponseBody.create(null, ""))
				.build();
		}).build();

		WikiDpsResolver resolver = new WikiDpsResolver(client);
		try
		{
			resolver.resolve("https://dps.osrs.wiki?id=HallowedVerzikPrison",
				HeartData.findTask("Dust devils"));
			fail("Redirect response should be rejected");
		}
		catch (IOException expected)
		{
			assertTrue(expected.getMessage().contains("301") || expected.getMessage().contains("302"));
		}

		assertEquals(1, requests.get());
		assertEquals("tools.runescape.wiki", requestedUrl.get().host());
		assertEquals("/osrs-dps/shortlink", requestedUrl.get().encodedPath());
		assertEquals("HallowedVerzikPrison", requestedUrl.get().queryParameter("id"));
	}
}
