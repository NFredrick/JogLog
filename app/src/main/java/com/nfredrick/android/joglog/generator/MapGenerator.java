package com.nfredrick.android.joglog.generator;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class MapGenerator {

    private static final double METERS_TO_MILES = 0.000621371;
    public static final String TAG = "com.nfredrick.android.joglog.generator.MapGenerator";
    private static final double MILES_TO_DEGREES = 0.01449; // approximately 69 miles in 1 degree latitude and longitude
    private static ListProperty mRoute;

    public static void calculateRoute(Location currentLocation, double distance, Context context, ListProperty route) {
        mRoute = route;
        StringProperty response = new StringProperty();
        response.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                Log.d(TAG, "got response");
                Map<MapNode, List<MapNode>> graph = parseResponse(response.getString());
                Log.d(TAG, "created graph");
                calculateRoute(currentLocation, graph, distance);
                Log.d(TAG, "route set");
            }
        });

        requestMapData(currentLocation, distance, context, response);
    }

    private static void setRoute(List<LatLng> calculatedRoute) {
        mRoute.setLst(new ArrayList<LatLng>(calculatedRoute));
    }

    private static void requestMapData(Location currentLocation, double distance, Context context, StringProperty response) {
        Log.d(TAG, "requestMapData");

        // get locations for bounding box
        double left = currentLocation.getLongitude() - distance * MILES_TO_DEGREES / 2.0;
        double right = currentLocation.getLongitude() + distance * MILES_TO_DEGREES / 2.0;
        double top = currentLocation.getLatitude() + distance * MILES_TO_DEGREES / 2.0;
        double bottom = currentLocation.getLatitude() - distance * MILES_TO_DEGREES / 2.0;

        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "https://www.overpass-api.de/api/interpreter?data=[out:json];way[highway]("
                + bottom + "," + left + "," + top + "," + right + ");out;node(w);out%20meta;";

        Log.d(TAG, "request url = " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "response received");
                        response.setString(res);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    Log.d(TAG, responseBody);

                } catch (UnsupportedEncodingException e) {
                    Log.d(TAG, e.toString());
                }
            }
        });
        Log.d(TAG, "sent request");
        queue.add(stringRequest);
    }

    private static Map<MapNode, List<MapNode>> parseResponse(String response) {
        Log.d(TAG, "parsing JSON response");
        Map<MapNode, List<MapNode>> graph = new HashMap<>();
        Map<Long, MapNode> idToNode = new HashMap<>();
        try {
            JSONObject json = new JSONObject(response);
            JSONArray data = (JSONArray) json.get("elements");

            Set<JSONObject> ways = new HashSet<>();

            Log.d(TAG, "parsing nodes");
            for (int i = 0; i < data.length(); i++) {
                JSONObject elem = (JSONObject) data.get(i);
                if (elem.get("type").equals("way")) {
                    ways.add((JSONObject) elem);
                } else if (elem.get("type").equals("node")) {
                    if (elem.get("id") instanceof Integer) {
                        int nodeId = (int) elem.get("id");
                        long nodeIdLong = (long) nodeId;
                        double lat = (double) elem.get("lat");
                        double lon = (double) elem.get("lon");
                        MapNode node = new MapNode(nodeIdLong, lat, lon);
                        if (!graph.containsKey(node)) {
                            graph.put(node, new ArrayList<>());
                        }
                        if (!idToNode.containsKey(node.getId())) {
                            idToNode.put(node.getId(), node);
                        }
                    } else if (elem.get("id") instanceof Long) {
                        long nodeId = (long) elem.get("id");
                        double lat = (double) elem.get("lat");
                        double lon = (double) elem.get("lon");
                        MapNode node = new MapNode(nodeId, lat, lon);
                        if (!graph.containsKey(node)) {
                            graph.put(node, new ArrayList<>());
                        }
                        if (!idToNode.containsKey(node.getId())) {
                            idToNode.put(node.getId(), node);
                        }
                    }
                }
            }

            Log.d(TAG, "found a total of " + graph.size() + " nodes and " + ways.size() + " ways");

            for (JSONObject obj : ways) {
                JSONArray nodeSequence = (JSONArray) obj.get("nodes");
                for (int i = 0; i < nodeSequence.length(); i++) {
                    long nodeId = -1;
                    if (nodeSequence.get(i) instanceof Integer) {
                        int nodeIdInt = (int) nodeSequence.get(i);
                        nodeId = (long) nodeIdInt;
                    } else if (nodeSequence.get(i) instanceof Long) {
                        nodeId = (long) nodeSequence.get(i);
                    }

                    if (!idToNode.containsKey(nodeId)) {
                        continue;
                    }

                    if (i - 1 >= 0) {
                        long prev = - 1;
                        if (nodeSequence.get(i-1) instanceof Integer) {
                            int prevIdInt = (int) nodeSequence.get(i-1);
                            prev = (long) prevIdInt;
                        } else if (nodeSequence.get(i-1) instanceof Long) {
                            prev = (long) nodeSequence.get(i-1);
                        }

                        if (idToNode.containsKey(prev)) {
                            graph.get(idToNode.get(nodeId)).add(idToNode.get(prev));
                            graph.get(idToNode.get(prev)).add(idToNode.get(nodeId));
                        }
                    }

                    if (i + 1 < nodeSequence.length()) {
                        long next = -1;
                        if (nodeSequence.get(i+1) instanceof Integer) {
                            int prevIdInt = (int) nodeSequence.get(i+1);
                            next = (long) prevIdInt;
                        } else if (nodeSequence.get(i+1) instanceof Long) {
                            next = (long) nodeSequence.get(i+1);
                        }

                        if (idToNode.containsKey(next)) {
                            graph.get(idToNode.get(nodeId)).add(idToNode.get(next));
                            graph.get(idToNode.get(next)).add(idToNode.get(nodeId));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }
        Log.d(TAG, "bottom");
        return graph;
    }

    private static List<LatLng> calculateRoute(Location currentLocation, Map<MapNode, List<MapNode>> graph, double target) {
        Log.d(TAG, "calculating route");
        MapNode source = startingLocation(currentLocation, graph);
        List<MapNode> route = new ArrayList<>();
        route.add(source);
        source.incrementVisit();
        calculateRoute(source, source, route, target, 0.0, 0.5, graph);
        Log.d(TAG, "route found");
        return route.stream()
                .map( mn -> new LatLng(mn.getLatitude(), mn.getLongitude()))
                .collect(Collectors.toList());
    }

    private static boolean calculateRoute(MapNode source, MapNode curr, List<MapNode> route, double target,
                                   double dist, double margin, Map<MapNode, List<MapNode>> graph) {
        Log.d(TAG, curr.toString());
        if (curr.equals(source) && withinTargetDistance(dist, target, margin)) {
            return true;
        } else if (dist > target + margin * dist) {
            return false;
        } else {
            for (MapNode next : graph.get(curr)) {
                if (next.getVisitCount() < 2) {
                    next.incrementVisit();
                    route.add(next);
                    float[] results = new float[3];
                    double addedDistance = results[0] * METERS_TO_MILES;
                    Location.distanceBetween(curr.getLatitude(), curr.getLongitude(), next.getLatitude(), next.getLongitude(), results);
                    if (calculateRoute(source, next, route, target, dist + addedDistance, margin, graph)) {
                        return true;
                    }
                    route.remove(route.size() - 1);
                    next.decrementVisit();
                }
            }
        }
        return false;
    }

    private static boolean withinTargetDistance(double currentDistance, double targetDistance, double margin) {
        return currentDistance > 0.1;
        // return Math.abs(currentDistance - targetDistance) <= targetDistance * margin;
    }

    private static MapNode startingLocation(Location currentLocation, Map<MapNode, List<MapNode>> graph) {
        Log.d(TAG, "Finding starting position");
        MapNode start = null;
        float dist = Float.MAX_VALUE;
        for (MapNode node : graph.keySet()) {
            float[] results = new float[3];
            Location.distanceBetween(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    node.getLatitude(), node.getLongitude(), results
            );
            if (results[0] < dist) {
                start = node;
                dist = results[0];
            }
        }
        return start;
    }
}
