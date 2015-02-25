package com.qiniu.processing;

    /*
NorthWest     |     North      |     NorthEast
              |                |
              |                |
--------------+----------------+--------------
              |                |
West          |     Center     |          East
              |                |
--------------+----------------+--------------
              |                |
              |                |
SouthWest     |     South      |     SouthEast
*/

public enum Gravity {
    NorthWest("NorthWest"), North("North"), NorthEast("NorthEast"), West("West"), Center("Center"), East("East"),
    SouthWest("SouthWest"), South("South"), SouthEast("SouthEast");
    private String gravity;

    private Gravity(String gravity) {
        this.gravity = gravity;
    }

    @Override
    public String toString() {
        return gravity;
    }
}
