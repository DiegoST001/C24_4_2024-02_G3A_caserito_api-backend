package com.tecsup.caserito_api.paq_modelo.paq_servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class GeocodingService {

    @Value("${google.maps.api.key}")
    private String GOOGLE_MAPS_API_KEY;

    public double[] getCoordinates(String ubicacion) {
        try {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                    ubicacion.replace(" ", "+") + "&key=" + GOOGLE_MAPS_API_KEY;

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            JSONObject jsonObject = new JSONObject(response);
            JSONObject location = jsonObject.getJSONArray("results")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONObject("location");

            double lat = location.getDouble("lat");
            double lng = location.getDouble("lng");

            return new double[]{lat, lng};
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener coordenadas: " + e.getMessage());
        }
    }

    public String getAddress(double lat, double lng) {
        try {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                    lat + "," + lng + "&key=" + GOOGLE_MAPS_API_KEY;

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            JSONObject jsonObject = new JSONObject(response);
            String address = jsonObject.getJSONArray("results")
                    .getJSONObject(0)
                    .getString("formatted_address");

            return address;
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener dirección: " + e.getMessage());
        }
    }
}

