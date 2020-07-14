import java.util.Arrays;

import DrivingInterface.DrivingInterface;

public class MyCar {

    boolean is_debug = false;

    
    public void control_driving(boolean a1, float a2, float a3, float a4, float a5, float a6, float a7, float a8,
                                float[] a9, float[] a10, float[] a11, float[] a12) {

        // ===========================================================
        // Don't remove this area. ===================================
        // ===========================================================
        DrivingInterface di = new DrivingInterface();
        DrivingInterface.CarStateValues sensing_info = di.get_car_state(a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11,a12);
        // ===========================================================

        if(is_debug) {
            System.out.println("=========================================================");
            System.out.println("[MyCar] to middle: " + sensing_info.to_middle);

            System.out.println("[MyCar] collided: " + sensing_info.collided);
            System.out.println("[MyCar] car speed: " + sensing_info.speed + "km/h");

            System.out.println("[MyCar] is moving forward: " + sensing_info.moving_forward);
            System.out.println("[MyCar] moving angle: " + sensing_info.moving_angle);
            System.out.println("[MyCar] lap_progress: " + sensing_info.lap_progress);

            StringBuilder forward_angles = new StringBuilder("[MyCar] track_forward_angles: ");
            for (Float track_forward_angle : sensing_info.track_forward_angles) {
                forward_angles.append(track_forward_angle).append(", ");
            }
            System.out.println(forward_angles);

            StringBuilder to_way_points = new StringBuilder("[MyCar] distance_to_way_points: ");
            for (Float distance_to_way_point : sensing_info.distance_to_way_points) {
                to_way_points.append(distance_to_way_point).append(", ");
            }
            System.out.println(to_way_points);

            StringBuilder forward_obstacles = new StringBuilder("[MyCar] track_forward_obstacles: ");
            for (DrivingInterface.ObstaclesInfo track_forward_obstacle : sensing_info.track_forward_obstacles) {
                forward_obstacles.append("{dist:").append(track_forward_obstacle.dist)
                        .append(", to_middle:").append(track_forward_obstacle.to_middle).append("}, ");
            }
            System.out.println(forward_obstacles);

            StringBuilder opponent_cars = new StringBuilder("[MyCar] opponent_cars_info: ");
            for (DrivingInterface.CarsInfo carsInfo : sensing_info.opponent_cars_info) {
                opponent_cars.append("{dist:").append(carsInfo.dist)
                        .append(", to_middle:").append(carsInfo.to_middle)
                        .append(", speed:").append(carsInfo.speed).append("km/h}, ");
            }
            System.out.println(opponent_cars);

            System.out.println("=========================================================");
        }

        // ===========================================================
        // Area for writing code about driving rule ==================
        // ===========================================================
        // Editing area starts from here
        //

        // Moving straight forward
        car_controls.steering = getSteer(sensing_info);
        car_controls.throttle = getAccel(sensing_info);
        car_controls.brake = 0;
        
        if(sensing_info.track_forward_obstacles.size() > 0){
	        float[] obs_middle = new float[sensing_info.track_forward_obstacles.size()];
	        int i=0;
	        float dist = sensing_info.track_forward_obstacles.get(0).to_middle;
	        for (DrivingInterface.ObstaclesInfo track_forward_obstacle : sensing_info.track_forward_obstacles) {
	        	if(dist - track_forward_obstacle.dist < 5){
	        		obs_middle[i++] = track_forward_obstacle.to_middle;
	        	}
	        }
	        
	        Arrays.sort(obs_middle);
	        
	        for (float tomiddle : obs_middle) {
	        	System.out.print(tomiddle + "   " );
	        }
	        System.out.println("");
	        
        }
        
        System.out.println(sensing_info.lap_progress + " : " +getDriveLine(sensing_info));
        
        
        if(is_debug) {
            System.out.println("[MyCar] steering:"+car_controls.steering+
                                     ", throttle:"+car_controls.throttle+", brake:"+car_controls.brake);
        }

        //
        // Editing area ends
        // =======================================================
    }
    
    float getDriveLine(DrivingInterface.CarStateValues sensing_info){
    	float driveLine = 0;
    	
        for (DrivingInterface.ObstaclesInfo track_forward_obstacle : sensing_info.track_forward_obstacles) {
            if(Math.abs(track_forward_obstacle.to_middle) <  3){
            	if(track_forward_obstacle.to_middle  < 1){
            		driveLine = 3.5f;
            	}else{
            		driveLine = -3.5f;
            	}
            }
            
        	if(sensing_info.track_forward_obstacles.size() > 1){
        		float dist = track_forward_obstacle.dist;
        		float left = track_forward_obstacle.to_middle;
        		float right = track_forward_obstacle.to_middle;
        		
        		boolean isMiddle = true;
        		for (DrivingInterface.ObstaclesInfo track_forward_obstacle2 : sensing_info.track_forward_obstacles) {
        			if(track_forward_obstacle2.dist - dist < 10){
        				if (left > track_forward_obstacle2.to_middle){
        					left = track_forward_obstacle2.to_middle;
        				}
        				if (right < track_forward_obstacle2.to_middle){
        					right = track_forward_obstacle2.to_middle;
        				}
        				if(Math.abs(track_forward_obstacle2.to_middle) < 2){
        					isMiddle = false;
        				}
        			}
        		}
        		
            	if((left+right)/2  < 1){
            		driveLine = 3.5f;
            	}else{
            		driveLine = -3.5f;
            	}
            	
            	if((left != right) && isMiddle ){
            		driveLine = 0;
            	}
            	
        	}
            
            break;
            
        }
    	
    	return driveLine;
    }
    
    float getSteer(DrivingInterface.CarStateValues sensing_info){
    	float steerAngle = -sensing_info.moving_angle/50;
    	
    	float steerMiddle = (getDriveLine(sensing_info)-sensing_info.to_middle)/((sensing_info.half_road_limit * 3)*2);
    	
    	
    	return steerAngle+steerMiddle;
    }
    
    float getAccel(DrivingInterface.CarStateValues sensing_info){
    	float accel = 0;
    	
    	if(sensing_info.speed < 60){
    		accel = 1;	
    	}else if(sensing_info.speed < 80){
    		accel = 0.8f;	
    	}else{
    		accel = 0.5f;
    	}
    	
    	
    	return accel;
    }

    // ===========================================================
    // Don't remove below area. ==================================
    // ===========================================================
    public native void StartDriving();

    static MyCar car_controls;

    float throttle;
    float steering;
    float brake;

    static {
        System.loadLibrary("DrivingInterface/DrivingInterface");
    }

    public static void main(String[] args) {
        car_controls = new MyCar();
        car_controls.StartDriving();
    }
    // ===========================================================
}
