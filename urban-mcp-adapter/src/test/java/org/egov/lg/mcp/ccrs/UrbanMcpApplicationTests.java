package org.egov.lg.mcp.ccrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.egov.lg.mcp.ccrs.models.ComplaintSummary;
import org.egov.lg.mcp.ccrs.utils.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UrbanMcpApplicationTests {

		@Autowired
		private UrbanMCPService tool;

		@Autowired
		private TokenService tokenSvc;

		@Test
		public void testLogin() {
			String result = tokenSvc.getCitizenToken();
			assertNotNull(result);
			assertFalse(result.isEmpty());
		}

		@Test
		public void testGetComplaints() {
			List<ComplaintSummary> result = tool.getComplaints("2025-04-01", "2025-08-31", "pb.amritsar", "0", "100");
			assertNotNull(result);
			assertFalse(result.isEmpty());
            assertEquals(100, result.size());
			//System.out.println(result.size());
		}

		@Test
		public void testGetComplaintCategories() throws JsonProcessingException {
			JsonNode result = tool.fetchComplaintCategories("pg");
			assertNotNull(result);
			assertFalse(result.isEmpty());
		}

	@Test
	public void testGetTenants() throws JsonProcessingException {
		List<UrbanMCPService.CityTenant> result = tool.fetchCityTenants();
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	public void testGetLocality() {
			List<UrbanMCPService.Locality> result = tool.fetchAddressLocality("pg.citya");
			assertNotNull(result);
			assertFalse(result.isEmpty());
	}

	@Test
	public void testFileComplaint() {
			ComplaintSummary summary = tool.createCitizenComplaint("pg.citya", "GarbageNeedsTobeCleared", "SUN01");
			assertNotNull(summary);
	}



}
