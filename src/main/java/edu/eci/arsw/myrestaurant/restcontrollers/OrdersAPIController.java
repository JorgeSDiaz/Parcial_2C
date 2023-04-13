/*
 * Copyright (C) 2016 Pivotal Software, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.eci.arsw.myrestaurant.restcontrollers;

import edu.eci.arsw.myrestaurant.beans.BillCalculator;
import edu.eci.arsw.myrestaurant.model.Order;
import edu.eci.arsw.myrestaurant.model.ProductType;
import edu.eci.arsw.myrestaurant.model.RestaurantProduct;
import edu.eci.arsw.myrestaurant.services.OrderServicesException;
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServices;
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServicesStub;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hcadavid
 */
@RestController
@RequestMapping(path = "/orders")
public class OrdersAPIController {
    @Autowired
    RestaurantOrderServices restaurantOrderServices;
    @Autowired
    BillCalculator billCalculator;

    @RequestMapping(path = "/test", method = RequestMethod.GET)
    public ResponseEntity<?> test() {
        return new ResponseEntity<String>("Works!", HttpStatus.OK);
    }

    @RequestMapping(path = "/", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getOrders() {
        try {
            String orders= "{";
            int position = 0;
            for (int key : restaurantOrderServices.getTablesWithOrders()) {
                String adding = "\"" + key + "\": {\"order\":";
                List<String> order = List.of(restaurantOrderServices.getTableOrder(key).toString().split(","));
                List<List<String>> listOrder = new ArrayList<>();
                for (int index = 0; index < order.size(); index++) {
                    List<String> tuple = List.of(order.get(index).split(" x "));
                    listOrder.add(List.of("\"" + tuple.get(0) + "\"", tuple.get(1)));
                }
                adding += listOrder.toString();
                adding += ", \"total\": " + restaurantOrderServices.calculateTableBill(key) + "}";
                orders += position < restaurantOrderServices.getTablesWithOrders().size() - 1 ? adding + ", " : adding;
                position++;
            }
            orders += "}";
            return new ResponseEntity<String>(orders, HttpStatus.OK);
        } catch (OrderServicesException e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
