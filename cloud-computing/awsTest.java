package aws;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Filter;

// 태그 추가
import com.amazonaws.services.ec2.model.Tag; 
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteTagsRequest;  

import com.amazonaws.services.cloudwatch.AmazonCloudWatch; 
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder; 
import com.amazonaws.services.cloudwatch.model.Datapoint; 
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest; 
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult; 
import com.amazonaws.services.cloudwatch.model.Statistic; 
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;  
import com.amazonaws.services.cloudwatch.model.StandardUnit;          
import com.amazonaws.services.logs.AWSLogs; 
import com.amazonaws.services.logs.AWSLogsClientBuilder; 
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest; 
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult;  
import com.amazonaws.services.cloudwatch.model.MetricAlarm;          
import com.amazonaws.services.cloudwatch.model.Dimension; 
import com.amazonaws.services.cloudwatch.model.DeleteAlarmsRequest;

public class awsTest {

	static AmazonEC2      ec2;
	
	static AmazonCloudWatch cloudWatch;
	static AWSLogs awsLogs; 

	private static void init() throws Exception {

		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
					"Please make sure that your credentials file is at the correct " +
					"location (~/.aws/credentials), and is in valid format.",
					e);
		}
		ec2 = AmazonEC2ClientBuilder.standard()
			.withCredentials(credentialsProvider)
			.withRegion("us-east-1")	
			.build();

		
		cloudWatch = AmazonCloudWatchClientBuilder.standard()
				.withCredentials(credentialsProvider)
				.withRegion("us-east-1")
				.build();

		: CloudWatch Logs 클라이언트 초기화
		awsLogs = AWSLogsClientBuilder.standard()
				.withCredentials(credentialsProvider)
				.withRegion("us-east-1")
				.build();

	}

	public static void main(String[] args) throws Exception {

		init();

		Scanner menu = new Scanner(System.in);
		Scanner id_string = new Scanner(System.in);
		int number = 0;
		
		while(true)
		{
			System.out.println("                                                              ");
			System.out.println("                                                              ");
			System.out.println("--------------------------------------------------------------");
			System.out.println("           Amazon AWS Control Panel using SDK                 ");
			System.out.println("--------------------------------------------------------------");
			System.out.println("  1. list instance                2. available zones          ");
			System.out.println("  3. start instance               4. available regions        ");
			System.out.println("  5. stop instance                6. create instance          ");
			System.out.println("  7. reboot instance              8. list images              ");
			System.out.println("  9. execute condor_status        10. list instances by tag   ");
			System.out.println(" 11. add tag to instance          12. delete tag from instance");
			System.out.println(" 30. get EC2 CPUUtilization metrics                           ");
			System.out.println(" 31. create CPU alarm for instance                            "); 
			System.out.println(" 33. check alarm state                                          "); 
			System.out.println(" 34. list alarms                                             "); 
			System.out.println(" 35. delete alarm                                            "); 
			System.out.println("                                  99. quit                     ");
			System.out.println("--------------------------------------------------------------");
			
			System.out.print("Enter an integer: ");
			
			if(menu.hasNextInt()){
				number = menu.nextInt();
				}else {
					System.out.println("concentration!");
					break;
				}
			

			String instance_id = "";

			switch(number) {
			case 1: 
				listInstances();
				break;
				
			case 2: 
				availableZones();
				break;
				
			case 3: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					startInstance(instance_id);
				break;

			case 4: 
				availableRegions();
				break;

			case 5: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					stopInstance(instance_id);
				break;

			case 6: 
				System.out.print("Enter ami id: ");
				String ami_id = "";
				if(id_string.hasNext())
					ami_id = id_string.nextLine();
				
				if(!ami_id.trim().isEmpty()) 
					createInstance(ami_id);
				break;

			case 7: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					rebootInstance(instance_id);
				break;

			case 8: 
				listImages();
				break;

			// condor_status 실행
			case 9: 
				System.out.print("Enter instance id: ");
				if (id_string.hasNext())
					instance_id = id_string.nextLine();
				if (!instance_id.trim().isEmpty())
					executeCondorStatus(instance_id);
				break;

				case 10:
				System.out.print("Enter tag key: ");
				String tagKey = id_string.nextLine().trim();
				
				System.out.print("Enter tag value: ");
				String tagValue = id_string.nextLine().trim();
				
				if (!tagKey.isEmpty() && !tagValue.isEmpty()) {
					listInstancesByTag(tagKey, tagValue);
				} else {
					System.out.println("Tag key or value is empty. Returning to main menu.");
				}
				break;
				
		
			case 11:
				System.out.print("Enter instance id: ");
				String tagAddInstanceId = id_string.nextLine().trim();
				System.out.print("Enter tag key: ");
				String addTagKey = id_string.nextLine().trim();
				System.out.print("Enter tag value: ");
				String addTagValue = id_string.nextLine().trim();
				
				if(!tagAddInstanceId.isEmpty() && !addTagKey.isEmpty() && !addTagValue.isEmpty()) {
					addTagToInstance(tagAddInstanceId, addTagKey, addTagValue);
				} else {
					System.out.println("Instance id or tag key/value is empty. Returning to main menu.");
				}
				break;
				
	
			case 12:
				System.out.print("Enter instance id: ");
				String tagDelInstanceId = id_string.nextLine().trim();
				System.out.print("Enter tag key: ");
				String delTagKey = id_string.nextLine().trim();
				System.out.print("Enter tag value: ");
				String delTagValue = id_string.nextLine().trim();
				
				if(!tagDelInstanceId.isEmpty() && !delTagKey.isEmpty() && !delTagValue.isEmpty()) {
					deleteTagFromInstance(tagDelInstanceId, delTagKey, delTagValue);
				} else {
					System.out.println("Instance id or tag key/value is empty. Returning to main menu.");
				}
				break;
		
				

			
			case 30:
				System.out.print("Enter instance id for CPU metric: ");
				String metricInstanceId = id_string.nextLine().trim();
				if (!metricInstanceId.isEmpty()) {
					getEC2CPUUtilization(metricInstanceId);
				} else {
					System.out.println("Instance id is empty. Returning to main menu.");
				}
				break;

			case 31: 
				System.out.print("Enter instance id for alarm: ");
				String alarmInstanceId = id_string.nextLine().trim();
				System.out.print("Enter alarm name: ");
				String alarmName = id_string.nextLine().trim();
				System.out.print("Enter CPU threshold (%): ");
				String thresholdStr = id_string.nextLine().trim();

				if (!alarmInstanceId.isEmpty() && !alarmName.isEmpty() && !thresholdStr.isEmpty()) {
					try {
						double threshold = Double.parseDouble(thresholdStr);
						createCPUAlarmForInstance(alarmInstanceId, alarmName, threshold);
					} catch (NumberFormatException e) {
						System.out.println("Invalid threshold input. Please enter a number.");
					}
				} else {
					System.out.println("Missing alarm parameters. Returning to main menu.");
				}
				break;

			case 33:
				System.out.print("Enter alarm name: ");
				String checkAlarmName = id_string.nextLine().trim();
				if (!checkAlarmName.isEmpty()) {
					checkAlarmState(checkAlarmName);
				} else {
					System.out.println("Alarm name is empty. Returning to main menu.");
				}
				break;

			case 34: 
				listAlarms();
				break;
			
			case 35: 
				System.out.print("Enter alarm name to delete: ");
				String alarmToDelete = id_string.nextLine().trim();
				if (!alarmToDelete.isEmpty()) {
					deleteAlarm(alarmToDelete);
				} else {
					System.out.println("Alarm name is empty. Returning to main menu.");
				}
				break;
			

			case 99: 
				System.out.println("bye!");
				menu.close();
				id_string.close();
				return;
			default: System.out.println("concentration!");
			}

		}
		
	}

	public static void listInstances() {
		
		System.out.println("Listing instances....");
		boolean done = false;
		
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					System.out.printf(
						"[id] %s, " +
						"[AMI] %s, " +
						"[type] %s, " +
						"[state] %10s, " +
						"[monitoring state] %s",
						instance.getInstanceId(),
						instance.getImageId(),
						instance.getInstanceType(),
						instance.getState().getName(),
						instance.getMonitoring().getState());

					
					if (instance.getTags() != null && !instance.getTags().isEmpty()) {
						System.out.print(", [tags] ");
						for (int i = 0; i < instance.getTags().size(); i++) {
							String tagKey = instance.getTags().get(i).getKey();
							String tagValue = instance.getTags().get(i).getValue();
							System.out.printf("%s=%s", tagKey, tagValue);
							if (i < instance.getTags().size() - 1) {
								System.out.print(", ");
							}
						}
					}
				}

				System.out.println();
			}

			request.setNextToken(response.getNextToken());

			if(response.getNextToken() == null) {
				done = true;
			}
		}
	}
	
	public static void availableZones()	{

		System.out.println("Available zones....");
		try {
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
			Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();
			
			AvailabilityZone zone;
			while(iterator.hasNext()) {
				zone = iterator.next();
				System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
			}
			System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
					" Availability Zones.");

		} catch (AmazonServiceException ase) {
				System.out.println("Caught Exception: " + ase.getMessage());
				System.out.println("Reponse Status Code: " + ase.getStatusCode());
				System.out.println("Error Code: " + ase.getErrorCode());
				System.out.println("Request ID: " + ase.getRequestId());
		}
	
	}

	public static void startInstance(String instance_id)
	{
		
		System.out.printf("Starting .... %s\n", instance_id);
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StartInstancesRequest> dry_request =
			() -> {
			StartInstancesRequest request = new StartInstancesRequest()
				.withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		StartInstancesRequest request = new StartInstancesRequest()
			.withInstanceIds(instance_id);

		ec2.startInstances(request);

		System.out.printf("Successfully started instance %s", instance_id);
	}
	
	
	public static void availableRegions() {
		
		System.out.println("Available regions ....");
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DescribeRegionsResult regions_response = ec2.describeRegions();

		for(Region region : regions_response.getRegions()) {
			System.out.printf(
				"[region] %15s, " +
				"[endpoint] %s\n",
				region.getRegionName(),
				region.getEndpoint());
		}
	}
	
	public static void stopInstance(String instance_id) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StopInstancesRequest> dry_request =
			() -> {
			StopInstancesRequest request = new StopInstancesRequest()
				.withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		try {
			StopInstancesRequest request = new StopInstancesRequest()
				.withInstanceIds(instance_id);
	
			ec2.stopInstances(request);
			System.out.printf("Successfully stop instance %s\n", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

	}
	
	public static void createInstance(String ami_id) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
		
		RunInstancesRequest run_request = new RunInstancesRequest()
			.withImageId(ami_id)
			.withInstanceType(InstanceType.T2Micro)
			.withMaxCount(1)
			.withMinCount(1);

		RunInstancesResult run_response = ec2.runInstances(run_request);

		String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

		System.out.printf(
			"Successfully started EC2 instance %s based on AMI %s",
			reservation_id, ami_id);
	
	}

	public static void rebootInstance(String instance_id) {
		
		System.out.printf("Rebooting .... %s\n", instance_id);
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		try {
			RebootInstancesRequest request = new RebootInstancesRequest()
					.withInstanceIds(instance_id);

				RebootInstancesResult response = ec2.rebootInstances(request);

				System.out.printf(
						"Successfully rebooted instance %s", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

		
	}
	
	public static void listImages() {
		System.out.println("Listing images....");
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
		
		DescribeImagesRequest request = new DescribeImagesRequest();
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		
		request.getFilters().add(new Filter().withName("name").withValues("aws-htcondor-slave"));
		request.setRequestCredentialsProvider(credentialsProvider);
		
		DescribeImagesResult results = ec2.describeImages(request);
		
		for(Image images :results.getImages()){
			System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n", 
					images.getImageId(), images.getName(), images.getOwnerId());
		}
		
	}





	private static String getPublicDns(String instanceId) {
        DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult response = ec2.describeInstances(request);

        for (Reservation reservation : response.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                return instance.getPublicDnsName(); // Return the Public DNS of the instance
            }
        }
        return null;
    }


    private static void executeCondorStatus(String instanceId) {
        try {
            String publicDns = getPublicDns(instanceId);
            if (publicDns == null || publicDns.isEmpty()) {
                System.out.println("Public DNS not found for instance: " + instanceId);
                return;
            }

            System.out.println("Connecting to instance: " + publicDns);


			ProcessBuilder processBuilder = new ProcessBuilder(
				"ssh", "-i", "key.pem",
				"-o", "StrictHostKeyChecking=no", 
				"ec2-user@" + publicDns,
				"condor_status"
			);

			
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("condor_status executed successfully.");
            } else {
                System.out.println("Error while executing condor_status. Exit code: " + exitCode);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


	// 태그
	public static void listInstancesByTag(String key, String value) {
		System.out.printf("Listing instances with tag [%s=%s]....\n", key, value);

		DescribeInstancesRequest request = new DescribeInstancesRequest()
				.withFilters(new Filter().withName("tag:" + key).withValues(value));

		boolean done = false;

		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			boolean found = false;
			for (Reservation reservation : response.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					System.out.printf("[id] %s, [AMI] %s, [type] %s, [state] %10s, [monitoring state] %s\n",
							instance.getInstanceId(), instance.getImageId(), instance.getInstanceType(),
							instance.getState().getName(), instance.getMonitoring().getState());
					found = true;
				}
			}

			if (!found) {
				System.out.println("No instances found with the given tag.");
			}

			request.setNextToken(response.getNextToken());
			if (response.getNextToken() == null) {
				done = true;
			}
		}
	}

	public static void addTagToInstance(String instanceId, String tagKey, String tagValue) {
		Tag tag = new Tag(tagKey, tagValue);
		CreateTagsRequest createTagsRequest = new CreateTagsRequest()
				.withResources(instanceId)
				.withTags(tag);
		ec2.createTags(createTagsRequest);
		System.out.printf("Successfully added tag [%s=%s] to instance %s\n", tagKey, tagValue, instanceId);
	}

	public static void deleteTagFromInstance(String instanceId, String tagKey, String tagValue) {
		Tag tag = new Tag(tagKey, tagValue);
		DeleteTagsRequest deleteTagsRequest = new DeleteTagsRequest()
				.withResources(instanceId)
				.withTags(tag);
		ec2.deleteTags(deleteTagsRequest);
		System.out.printf("Successfully deleted tag [%s=%s] from instance %s\n", tagKey, tagValue, instanceId);
	}




	public static void getEC2CPUUtilization(String instanceId) {
		System.out.println("Getting CPU Utilization for instance: " + instanceId);
		
		long offsetInMilliseconds = 1000 * 60 * 60; // 1 hour
		Date endTime = new Date();
		Date startTime = new Date(endTime.getTime() - offsetInMilliseconds);

		GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
				.withStartTime(startTime)
				.withEndTime(endTime)
				.withPeriod(300) // 5분 간격 (300초)
				.withMetricName("CPUUtilization")
				.withNamespace("AWS/EC2")
				.withStatistics(Statistic.Average)
				.withDimensions(new com.amazonaws.services.cloudwatch.model.Dimension()
						.withName("InstanceId")
						.withValue(instanceId));

		GetMetricStatisticsResult getMetricStatisticsResult = cloudWatch.getMetricStatistics(request);
		List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();

		if (datapoints.isEmpty()) {
			System.out.println("No CPU utilization data found for the given time period.");
		} else {
			for (Datapoint dp : datapoints) {
				System.out.printf("Time: %s, Average CPU Utilization: %.2f%%\n", dp.getTimestamp().toString(), dp.getAverage());
			}
		}
	}


	public static void createCPUAlarmForInstance(String instanceId, String alarmName, double threshold) {
		System.out.printf("Creating alarm: %s for instance: %s with threshold: %.2f%%\n", alarmName, instanceId, threshold);

		PutMetricAlarmRequest putMetricAlarmRequest = new PutMetricAlarmRequest()
				.withAlarmName(alarmName)
				.withMetricName("CPUUtilization")
				.withNamespace("AWS/EC2")
				.withStatistic(Statistic.Average)
				.withPeriod(300)
				.withEvaluationPeriods(1)
				.withThreshold(threshold)
				.withComparisonOperator("GreaterThanThreshold")
				.withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
				.withUnit(StandardUnit.Percent);

		cloudWatch.putMetricAlarm(putMetricAlarmRequest);
		System.out.println("Alarm created successfully. Check CloudWatch console for the alarm status.");
	}


	public static void checkAlarmState(String alarmName) {
		System.out.println("Checking alarm state for: " + alarmName);
		DescribeAlarmsRequest request = new DescribeAlarmsRequest().withAlarmNames(alarmName);
		DescribeAlarmsResult result = cloudWatch.describeAlarms(request);

		if (!result.getMetricAlarms().isEmpty()) {
			MetricAlarm alarm = result.getMetricAlarms().get(0);
			String state = alarm.getStateValue();
			System.out.println("Alarm State: " + state);
			if (state.equalsIgnoreCase("ALARM")) {
				System.out.println("**ALARM TRIGGERED** The metric has exceeded the threshold!");
			}
		} else {
			System.out.println("Alarm not found.");
		}
	}


	public static void listAlarms() {
		System.out.println("Listing all alarms...");
		DescribeAlarmsRequest request = new DescribeAlarmsRequest();
		DescribeAlarmsResult result = cloudWatch.describeAlarms(request);

		List<MetricAlarm> alarms = result.getMetricAlarms();
		if (alarms.isEmpty()) {
			System.out.println("No alarms found.");
		} else {
			for (MetricAlarm alarm : alarms) {
				System.out.printf("Alarm Name: %s, State: %s, Metric: %s, Threshold: %.2f\n",
						alarm.getAlarmName(),
						alarm.getStateValue(),
						alarm.getMetricName(),
						alarm.getThreshold());
			}
		}
	}


	public static void deleteAlarm(String alarmName) {
		System.out.printf("Deleting alarm: %s\n", alarmName);
		cloudWatch.deleteAlarms(new DeleteAlarmsRequest().withAlarmNames(alarmName));
		System.out.println("Alarm deleted successfully.");
	}

}
	