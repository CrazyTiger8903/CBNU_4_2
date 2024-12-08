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
import com.amazonaws.services.ec2.model.Tag; 
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteTagsRequest;  
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch; 
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder; 
import com.amazonaws.services.cloudwatch.model.Datapoint; 
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult; 
import com.amazonaws.services.cloudwatch.model.Statistic; 
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest; 
import com.amazonaws.services.cloudwatch.model.StandardUnit;  
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest; 
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult;  
import com.amazonaws.services.cloudwatch.model.MetricAlarm;        
import com.amazonaws.services.cloudwatch.model.Dimension; 
import com.amazonaws.services.cloudwatch.model.DeleteAlarmsRequest;
import com.amazonaws.services.logs.AWSLogs; 
import com.amazonaws.services.logs.AWSLogsClientBuilder; 


public class awsTest {

	static AmazonEC2 ec2;
	static AmazonCloudWatch cloudWatch;
	static AWSLogs awsLogs;

	static final String SNS_TOPIC_ARN = "arn";

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
		
		// EC2 클라이언트 초기화
		ec2 = AmazonEC2ClientBuilder.standard()
			.withCredentials(credentialsProvider)
			.withRegion("us-east-1")	/* check the region at AWS console */
			.build();

		// CloudWatch 클라이언트 초기화
		cloudWatch = AmazonCloudWatchClientBuilder.standard()
				.withCredentials(credentialsProvider)
				.withRegion("us-east-1")
				.build();

		// CloudWatch Logs 클라이언트 초기화
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
		
		while(true) {
			System.out.println();
			System.out.println("==============================================================");
			System.out.println("                Amazon AWS Control Panel using SDK            ");
			System.out.println("==============================================================");
		
			// EC2 인스턴스 관리 섹션
			System.out.println("  [ EC2 Instance Management ]");
			System.out.println("  1.  List instances               2.  Available zones");
			System.out.println("  3.  Start instance               4.  Available regions");
			System.out.println("  5.  Stop instance                6.  Create instance");
			System.out.println("  7.  Reboot instance              8.  List images");
			System.out.println();
		
			// 태그 관리 섹션
			System.out.println("  [ Tag Management ]");
			System.out.println("  9.  Execute condor_status        10. List instances by tag");
			System.out.println("  11. Add tag to instance          12. Delete tag from instance");
			System.out.println();
		
			// CPU 및 알람 섹션
			System.out.println("  [ CPU & Alarm Management ]");
			System.out.println("  30. Get EC2 CPUUtilization metrics");
			System.out.println("  31. Create CPU alarm for instance");
			System.out.println("  32. Check alarm state");
			System.out.println("  33. List alarms");
			System.out.println("  34. Delete alarm");
			System.out.println();
		
			// EBS 볼륨 및 스냅샷 관리 섹션
			System.out.println("  [ EBS Volume & Snapshot Management ]");
			System.out.println("  40. List volumes                 41. Create volume");
			System.out.println("  42. Attach volume to instance    43. Detach volume");
			System.out.println("  44. Delete volume                45. Create snapshot");
			System.out.println("  46. List snapshots               47. Delete snapshot");
			System.out.println("  48. Create volume from snapshot");
			System.out.println();
		
			// 종료 옵션
			System.out.println("  [ Exit ]");
			System.out.println("  99. Quit");
			System.out.println("==============================================================");
			System.out.print("Enter an integer: ");
			
			if(menu.hasNextInt()){
				number = menu.nextInt();
				}else {
					System.out.println("concentration!");
					break;
				}
			

			String instance_id = "";

			switch(number) {
			/******************************************1. EC2 인스턴스 관리**********************************/	
			// 인스턴스 목록 조회
			case 1:    
				listInstances();
				break;

			// 사용 가능한 영역 조회
			case 2:  
				availableZones();
				break;

			// 특정 인스턴스 시작
			case 3: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					startInstance(instance_id);
				break;

			// 사용 가능한 리전(region) 조회
			case 4: 
				availableRegions();
				break;

			// 특정 인스턴스 중지
			case 5: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					stopInstance(instance_id);
				break;
			
			// 새 인스턴스 생성
			case 6: 
				System.out.print("Enter ami id: ");
				String ami_id = "";
				if(id_string.hasNext())
					ami_id = id_string.nextLine();
				
				if(!ami_id.trim().isEmpty()) 
					createInstance(ami_id);
				break;

			// 특정 인스턴스 재부팅
			case 7: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					rebootInstance(instance_id);
				break;

			// 사용 가능한 AMI 목록 조회
			case 8: 
				listImages();
				break;

			// 특정 인스턴스에서 condor_status 실행
			case 9: 
				System.out.print("Enter instance id: ");
				if (id_string.hasNext())
					instance_id = id_string.nextLine();
				if (!instance_id.trim().isEmpty())
					executeCondorStatus(instance_id);
				break;

			/******************************************2. 태그 관리**********************************/	
			// 특정 태그를 가진 인스턴스 목록 조회	
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
		

			// 특정 인스턴스에 태그 추가
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
				
			// 특정 인스턴스에서 태그 삭제
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
				
			/*************************************3. CloudWatch 메트릭 및 알람**********************************/	

			// 특정 인스턴스의 CPU 사용량 메트릭 조회
			case 30:
				System.out.print("Enter instance id for CPU metric: ");
				String metricInstanceId = id_string.nextLine().trim();
				if (!metricInstanceId.isEmpty()) {
					getEC2CPUUtilization(metricInstanceId);
				} else {
					System.out.println("Instance id is empty. Returning to main menu.");
				}
				break;

			// 특정 인스턴스의 CPU 알람 생성	
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

			// 특정 알람의 상태 조회
			case 32: 
				System.out.print("Enter alarm name: ");
				String checkAlarmName = id_string.nextLine().trim();
				if (!checkAlarmName.isEmpty()) {
					checkAlarmState(checkAlarmName);
				} else {
					System.out.println("Alarm name is empty. Returning to main menu.");
				}
				break;

			// 모든 알람 조회
			case 33: 
				listAlarms();
				break;
			
			// 특정 알람 삭제	
			case 34: 
				System.out.print("Enter alarm name to delete: ");
				String alarmToDelete = id_string.nextLine().trim();
				if (!alarmToDelete.isEmpty()) {
					deleteAlarm(alarmToDelete);
				} else {
					System.out.println("Alarm name is empty. Returning to main menu.");
				}
				break;
			
			/************************************4. EBS 볼륨 및 스냅샷 관리**********************************/	
			// 모든 볼륨 목록 조회
			case 40:
				listVolumes();
				break;

			// 새 볼륨 생성	
			case 41:
				System.out.print("Enter availability zone (e.g., us-east-1a): ");
				String az = id_string.nextLine().trim();
				System.out.print("Enter volume size in GiB: ");
				String sizeStr = id_string.nextLine().trim();
				if(!az.isEmpty() && !sizeStr.isEmpty()) {
					try {
						int sizeGiB = Integer.parseInt(sizeStr);
						createVolume(az, sizeGiB);
					} catch(NumberFormatException e) {
						System.out.println("Invalid size input.");
					}
				} else {
					System.out.println("Missing parameters.");
				}
				break;

			// 볼륨을 특정 인스턴스에 연결
			case 42:
				System.out.print("Enter volume id: ");
				String volIdAttach = id_string.nextLine().trim();
				System.out.print("Enter instance id: ");
				String instIdAttach = id_string.nextLine().trim();
				System.out.print("Enter device name (e.g. /dev/sdf): ");
				String device = id_string.nextLine().trim();
				if(!volIdAttach.isEmpty() && !instIdAttach.isEmpty() && !device.isEmpty()) {
					attachVolume(volIdAttach, instIdAttach, device);
				} else {
					System.out.println("Missing parameters.");
				}
				break;

			// 볼륨 분리
			case 43:
				System.out.print("Enter volume id to detach: ");
				String volIdDetach = id_string.nextLine().trim();
				if(!volIdDetach.isEmpty()) {
					detachVolume(volIdDetach);
				} else {
					System.out.println("Volume id is empty.");
				}
				break;

			// 볼륨 삭제
			case 44:
				System.out.print("Enter volume id to delete: ");
				String volIdDelete = id_string.nextLine().trim();
				if(!volIdDelete.isEmpty()) {
					deleteVolume(volIdDelete);
				} else {
					System.out.println("Volume id is empty.");
				}
				break;

			// 스냅샷 생성
			case 45:
				System.out.print("Enter volume id to snapshot: ");
				String volIdSnap = id_string.nextLine().trim();
				System.out.print("Enter snapshot description: ");
				String snapDesc = id_string.nextLine().trim();
				if(!volIdSnap.isEmpty() && !snapDesc.isEmpty()) {
					createSnapshot(volIdSnap, snapDesc);
				} else {
					System.out.println("Missing parameters.");
				}
				break;

			// 스냅샷 목록 조회
			case 46:
				listSnapshots();
				break;

			// 스냅샷 삭제
			case 47:
				System.out.print("Enter snapshot id to delete: ");
				String snapIdDel = id_string.nextLine().trim();
				if(!snapIdDel.isEmpty()) {
					deleteSnapshot(snapIdDel);
				} else {
					System.out.println("Snapshot id is empty.");
				}
				break;

			// 스냅샷 기반 볼륨 생성
			case 48:
				System.out.print("Enter snapshot id to create volume from: ");
				String snapIdVol = id_string.nextLine().trim();
				System.out.print("Enter availability zone: ");
				String azVol = id_string.nextLine().trim();
				if(!snapIdVol.isEmpty() && !azVol.isEmpty()) {
					createVolumeFromSnapshot(snapIdVol, azVol);
				} else {
					System.out.println("Missing parameters.");
				}
				break;

			// 프로그램 종료	
			case 99: 
				System.out.println("bye!");
				menu.close();
				id_string.close();
				return;
			
			default: System.out.println("concentration!");
			}
		}
	}

	/******************************************1. EC2 인스턴스 관리**********************************/	
	// 현재 AWS 계정과 리전에 존재하는 모든 EC2 인스턴스의 목록을 출력하는 메서드.
	// 인스턴스 ID, AMI, 타입, 상태, 모니터링 상태 등의 기본 정보와 태그(Key=Value 형태) 리스트를 함께 출력한다.
	// 태그도 나오도록 수정함.
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

					// 태그가 존재한다면 태그 출력
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
	
	// 현재 리전(Region)에서 사용 가능한 가용 영역(Availability Zone) 목록을 조회하는 메서드.
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

	// 주어진 EC2 인스턴스 ID를 사용하여 해당 인스턴스를 시작하는 메서드.
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
	
	// AWS에서 사용 가능한 모든 리전(Region)을 출력하는 메서드.
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
	
	// 주어진 EC2 인스턴스를 중지(stop)하는 메서드.
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
	
	// 주어진 AMI ID를 기반으로 새 EC2 인스턴스를 생성하는 메서드.
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


	// 주어진 인스턴스 ID를 사용하여 EC2 인스턴스를 재부팅하는 메서드.
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
	
	// EC2 AMI (Amazon Machine Image) 목록을 조회하는 메서드.
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


	/************************************************추가 부분*************************************************/
	// 과제2. condor_status 추가
	
	// 주어진 EC2 인스턴스 ID에 해당하는 Public DNS를 반환하는 메서드.
	private static String getPublicDns(String instanceId) {
		// EC2 인스턴스 ID를 기준으로 DescribeInstances 요청 생성
        DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult response = ec2.describeInstances(request);

		// 응답으로부터 Reservation과 Instance 정보를 순회하며 Public DNS 검색
        for (Reservation reservation : response.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                return instance.getPublicDnsName(); // Public DNS 반환
            }
        }
        return null;
    }

    // 주어진 EC2 인스턴스에서 condor_status 명령을 실행하는 메서드.
    private static void executeCondorStatus(String instanceId) {
        try {
			// EC2 인스턴스의 Public DNS 가져오기
            String publicDns = getPublicDns(instanceId);
            if (publicDns == null || publicDns.isEmpty()) {
                System.out.println("Public DNS not found for instance: " + instanceId);
                return;
            }
            System.out.println("Connecting to instance: " + publicDns);
			// SSH 명령 실행을 위한 ProcessBuilder 설정

			ProcessBuilder processBuilder = new ProcessBuilder(
				"ssh", "-i", ".pem",
				"-o", "StrictHostKeyChecking=no",
				"ec2-user@" + publicDns,
				"condor_status"
			);
						
            processBuilder.redirectErrorStream(true);
			// SSH 프로세스 실행
            Process process = processBuilder.start();

			// 명령어 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

			// 프로세스 종료 코드 확인
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

	/******************************************2. 태그 관리**********************************/	
	// 특정 태그 키와 값을 기준으로 EC2 인스턴스 목록을 필터링하여 출력하는 메서드.
	public static void listInstancesByTag(String key, String value) {
		System.out.printf("Listing instances with tag [%s=%s]....\n", key, value);

		// DescribeInstancesRequest에 필터 추가: 태그 키와 값으로 검색
		DescribeInstancesRequest request = new DescribeInstancesRequest()
				.withFilters(new Filter().withName("tag:" + key).withValues(value));

		boolean done = false; // 다음 페이지가 있는지 확인하기 위한 플래그

		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			boolean found = false; // 인스턴스 존재 여부를 확인하기 위한 변수
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

			// 다음 페이지가 있는지 확인하고 요청 갱신
			request.setNextToken(response.getNextToken());
			if (response.getNextToken() == null) {
				done = true;
			}
		}
	}

	// 특정 EC2 인스턴스에 태그를 추가하는 메서드.
	public static void addTagToInstance(String instanceId, String tagKey, String tagValue) {
		// 태그 객체 생성
		Tag tag = new Tag(tagKey, tagValue);
		// CreateTagsRequest 생성
		CreateTagsRequest createTagsRequest = new CreateTagsRequest()
				.withResources(instanceId) // 대상 리소스 설정 (여기서는 인스턴스 ID)
				.withTags(tag);            // 추가할 태그 설정
		// AWS EC2 클라이언트를 사용해 태그 추가 요청 실행
		ec2.createTags(createTagsRequest);
		System.out.printf("Successfully added tag [%s=%s] to instance %s\n", tagKey, tagValue, instanceId);
	}

	// 특정 EC2 인스턴스에서 태그를 삭제하는 메서드.
	public static void deleteTagFromInstance(String instanceId, String tagKey, String tagValue) {
		// 태그 객체 생성ㄴ
		Tag tag = new Tag(tagKey, tagValue);
		// DeleteTagsRequest 생성
		DeleteTagsRequest deleteTagsRequest = new DeleteTagsRequest()
				.withResources(instanceId)
				.withTags(tag);
		// AWS EC2 클라이언트를 사용해 태그 삭제 요청 실행
		ec2.deleteTags(deleteTagsRequest);
		System.out.printf("Successfully deleted tag [%s=%s] from instance %s\n", tagKey, tagValue, instanceId);
	}

	/*************************************3. CloudWatch 메트릭 및 알람**********************************/	
	
	// 특정 EC2 인스턴스의 지난 시간 동안 CPU Utilization 메트릭을 조회하는 메서드.
	public static void getEC2CPUUtilization(String instanceId) {
		System.out.println("Getting CPU Utilization for instance: " + instanceId);
		
		// 지난 1시간 동안의 데이터를 조회하기 위해 시간 설정
		long offsetInMilliseconds = 1000 * 60 * 60; // 1 hour
		Date endTime = new Date();
		Date startTime = new Date(endTime.getTime() - offsetInMilliseconds); // 1시간 전

		// CloudWatch 메트릭 조회 요청 생성
		GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
				.withStartTime(startTime)          // 시작 시간
				.withEndTime(endTime)              // 종료 시간
				.withPeriod(300)                   // 5분 간격 (300초)
				.withMetricName("CPUUtilization")  // 조회할 메트릭 이름
				.withNamespace("AWS/EC2")          // 네임스페이스 (AWS/EC2)
				.withStatistics(Statistic.Average) // 통계 유형: 평균
				.withDimensions(new com.amazonaws.services.cloudwatch.model.Dimension()
						.withName("InstanceId")    // 필터 조건: 인스턴스 ID
						.withValue(instanceId));

		// CloudWatch 클라이언트로 메트릭 데이터 조회
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


	// 특정 EC2 인스턴스에 대해 CPU 사용률 기준의 CloudWatch 알람을 생성하는 메서드.
	// CPU 사용률이 지정된 임계값(threshold)을 초과할 경우 SNS 알림이 트리거된다.(이메일 발송)
	public static void createCPUAlarmForInstance(String instanceId, String alarmName, double threshold) {
		System.out.printf("Creating alarm: %s for instance: %s with threshold: %.2f%%\n", alarmName, instanceId, threshold);

		// CloudWatch 알람 생성 요청 구성
		PutMetricAlarmRequest putMetricAlarmRequest = new PutMetricAlarmRequest()
				.withAlarmName(alarmName)                              // 알람 이름
				.withMetricName("CPUUtilization")                      // 모니터링할 메트릭: CPUUtilization
				.withNamespace("AWS/EC2")                              // 메트릭 네임스페이스
				.withStatistic(Statistic.Average)                      // 통계 유형: 평균
				.withPeriod(300)                                       // 평가 주기: 5분 (300초)
				.withEvaluationPeriods(1)                              // 조건 평가 횟수: 1번 초과 시 알람 발생
				.withThreshold(threshold)                              // 임계값: CPU 사용률(%)
				.withComparisonOperator("GreaterThanThreshold")        // 비교 연산자: 임계값 초과
				.withDimensions(new Dimension()
						.withName("InstanceId")                        // 메트릭 조건: 대상 인스턴스 ID
						.withValue(instanceId))
				.withUnit(StandardUnit.Percent)                        // 단위: 퍼센트
				.withAlarmActions(SNS_TOPIC_ARN);                      // 알람 트리거 시 수행할 액션: SNS 알림

		// CloudWatch에 알람 요청 전송
		cloudWatch.putMetricAlarm(putMetricAlarmRequest);
		System.out.println("Alarm created successfully. Check CloudWatch console and your email for the alarm.");
	}

	// 지정된 알람 이름을 기반으로 CloudWatch에서 알람 상태를 확인
	// 알람이 "ALARM" 상태로 전환되었을 경우 알람 액션을 비활성화하는 메서드.
	public static void checkAlarmState(String alarmName) {
		System.out.println("Checking alarm state for: " + alarmName);

		// CloudWatch 알람 조회 요청 생성
		DescribeAlarmsRequest request = new DescribeAlarmsRequest().withAlarmNames(alarmName);
		// CloudWatch에서 알람 정보를 가져옴
		DescribeAlarmsResult result = cloudWatch.describeAlarms(request);

		// 알람 정보가 있을 경우 처리
		if (!result.getMetricAlarms().isEmpty()) {
			MetricAlarm alarm = result.getMetricAlarms().get(0);  // 첫 번째 알람 정보 가져오기
			String state = alarm.getStateValue();  // 알람 상태 값 (OK, ALARM, INSUFFICIENT_DATA 등)
			System.out.println("Alarm State: " + state);
			// 알람 상태가 "ALARM"인 경우
			if (state.equalsIgnoreCase("ALARM")) {
				System.out.println("**ALARM TRIGGERED** The metric has exceeded the threshold!");
				// 최초 ALARM 상태 진입 시 알람 액션 비활성화
				disableAlarmActions(alarmName, alarm);
			}
		} else {
			System.out.println("Alarm not found.");
		}
	}

	// 특정 CloudWatch 알람에서 액션(예: SNS 알림)을 제거하는 메서드.
	public static void disableAlarmActions(String alarmName, MetricAlarm alarm) {
		// 기존 알람 설정을 기반으로 새로운 요청 생성
		PutMetricAlarmRequest putRequest = new PutMetricAlarmRequest()
			.withAlarmName(alarmName) // 알람 이름
			.withMetricName(alarm.getMetricName()) // 메트릭 이름
			.withNamespace(alarm.getNamespace()) // 네임스페이스 (AWS/EC2 등)
			.withStatistic(alarm.getStatistic()) // 통계 유형 (예: 평균값)
			.withPeriod(alarm.getPeriod()) // 평가 주기(초)
			.withEvaluationPeriods(alarm.getEvaluationPeriods()) // 평가 기간(몇 주기 동안 조건 확인)
			.withThreshold(alarm.getThreshold()) // 임계값
			.withComparisonOperator(alarm.getComparisonOperator()) // 비교 연산자 (예: GreaterThanThreshold)
			.withDimensions(alarm.getDimensions()) // 메트릭 차원 (예: 인스턴스 ID)
			.withUnit(alarm.getUnit()); // 단위 (예: Percent)

		// 여기서 AlarmActions를 빈 리스트로 설정해 메일 액션을 제거한다.
		// 즉, 다음 번에 ALARM 상태로 바뀌어도 메일은 안 감.
		putRequest.setAlarmActions(null);

		// CloudWatch에 수정된 알람 요청 전송
		cloudWatch.putMetricAlarm(putRequest);
		System.out.println("Alarm actions disabled after first ALARM trigger.");
	}	

	// CloudWatch에 설정된 모든 알람을 조회하고 출력하는 메서드.
	public static void listAlarms() {
		System.out.println("Listing all alarms...");
		// DescribeAlarmsRequest 객체 생성
		DescribeAlarmsRequest request = new DescribeAlarmsRequest();
		// CloudWatch에서 알람 정보 요청
		DescribeAlarmsResult result = cloudWatch.describeAlarms(request);

		// 결과에서 MetricAlarm 객체 리스트를 가져옴
		List<MetricAlarm> alarms = result.getMetricAlarms();

		if (alarms.isEmpty()) {
			System.out.println("No alarms found.");
		} else {
			// 알람 정보 출력
			for (MetricAlarm alarm : alarms) {
				System.out.printf("Alarm Name: %s, State: %s, Metric: %s, Threshold: %.2f\n",
						alarm.getAlarmName(),
						alarm.getStateValue(),
						alarm.getMetricName(),
						alarm.getThreshold());
			}
		}
	}

	// CloudWatch에서 특정 알람을 삭제하는 메서드.
	public static void deleteAlarm(String alarmName) {
		System.out.printf("Deleting alarm: %s\n", alarmName);
		cloudWatch.deleteAlarms(new DeleteAlarmsRequest().withAlarmNames(alarmName));
		System.out.println("Alarm deleted successfully.");
	}


	/************************************4. EBS 볼륨 및 스냅샷 관리**********************************/	
	
	// 현재 계정에서 관리 중인 모든 EBS 볼륨 목록을 조회하여 출력하는 메서드.
	public static void listVolumes() {
		System.out.println("Listing EBS volumes...");
		// DescribeVolumesRequest를 이용해 볼륨 정보를 요청
		DescribeVolumesResult result = ec2.describeVolumes(new DescribeVolumesRequest());
		// 응답에서 볼륨 목록 가져오기
		List<Volume> volumes = result.getVolumes();
		if (volumes.isEmpty()) {
			System.out.println("No volumes found.");
		} else {
			// 각 볼륨의 정보를 출력
			for (Volume vol : volumes) {
				System.out.printf("VolumeId: %s, Size: %dGB, State: %s, Type: %s, AZ: %s\n",
						vol.getVolumeId(),     // 볼륨 ID
						vol.getSize(),         // 볼륨 크기(GB)
						vol.getState(),        // 볼륨 상태
						vol.getVolumeType(),   // 볼륨 유형 (예: gp2, io1 등)
						vol.getAvailabilityZone());  // 볼륨의 가용 영역
			}
		}
	}

	// 지정된 가용 영역과 크기로 새로운 EBS 볼륨을 생성하는 메서드.
	public static void createVolume(String availabilityZone, int sizeGiB) {
		System.out.printf("Creating volume in AZ: %s, Size: %dGB\n", availabilityZone, sizeGiB);
		// EBS 볼륨 생성 요청 구성
		CreateVolumeRequest request = new CreateVolumeRequest()
				.withAvailabilityZone(availabilityZone)
				.withSize(sizeGiB)
				.withVolumeType("gp2");
		// 생성된 볼륨 객체 반환
		Volume volume = ec2.createVolume(request).getVolume();
		// 생성된 볼륨 ID 출력
		System.out.printf("Created volume: %s\n", volume.getVolumeId());
	}

	// EBS 볼륨을 특정 EC2 인스턴스에 연결하는 메서드
	public static void attachVolume(String volumeId, String instanceId, String device) {
		System.out.printf("Attaching volume %s to instance %s at device %s\n", volumeId, instanceId, device);
		// 볼륨 연결 요청 생성
		AttachVolumeRequest request = new AttachVolumeRequest()
				.withVolumeId(volumeId)
				.withInstanceId(instanceId)
				.withDevice(device);
		// AWS EC2 클라이언트를 이용해 볼륨 연결 요청 실행
		ec2.attachVolume(request);
		System.out.println("Volume attached successfully.");
	}

	// EBS 볼륨을 특정 EC2 인스턴스에서 분리하는 메서드
	public static void detachVolume(String volumeId) {
		System.out.printf("Detaching volume %s\n", volumeId);
		// 볼륨 분리 요청 생성
		DetachVolumeRequest request = new DetachVolumeRequest()
				.withVolumeId(volumeId);
		// AWS EC2 클라이언트를 이용해 볼륨 분리 요청 실행
		ec2.detachVolume(request);
		System.out.println("Volume detached successfully.");
	}

	// EBS 볼륨을 삭제하는 메서드
	public static void deleteVolume(String volumeId) {
		System.out.printf("Deleting volume %s\n", volumeId);
		// 볼륨 삭제 요청 생성
		DeleteVolumeRequest request = new DeleteVolumeRequest().withVolumeId(volumeId);
		// AWS EC2 클라이언트를 이용해 볼륨 삭제 요청 실행
		ec2.deleteVolume(request);
		System.out.println("Volume deleted successfully.");
	}

	// EBS 볼륨의 스냅샷을 생성하는 메서드
	public static void createSnapshot(String volumeId, String description) {
		System.out.printf("Creating snapshot for volume %s with description '%s'\n", volumeId, description);
		// 스냅샷 생성 요청 생성
		CreateSnapshotRequest request = new CreateSnapshotRequest()
				.withVolumeId(volumeId)
				.withDescription(description);
		// AWS EC2 클라이언트를 통해 스냅샷 생성 요청 실행
		String snapshotId = ec2.createSnapshot(request).getSnapshot().getSnapshotId();
		System.out.printf("Created snapshot: %s\n", snapshotId);
	}

	// 현재 계정에서 소유한 모든 EBS 스냅샷을 조회하여 출력하는 메서드
	public static void listSnapshots() {
		System.out.println("Listing snapshots...");
		// DescribeSnapshotsRequest 생성
		DescribeSnapshotsRequest request = new DescribeSnapshotsRequest().withOwnerIds("self");
	
		// 스냅샷 정보를 요청하고 결과 가져오기
		List<Snapshot> snapshots = ec2.describeSnapshots(request).getSnapshots();

		if (snapshots.isEmpty()) {
			System.out.println("No snapshots found.");
		} else {
			// 각 스냅샷 정보를 출력
			for (Snapshot snap : snapshots) {
				System.out.printf("SnapshotId: %s, VolumeId: %s, State: %s, StartTime: %s\n",
						snap.getSnapshotId(),  // 스냅샷 ID
						snap.getVolumeId(),    // 스냅샷이 생성된 볼륨 ID
						snap.getState(),       // 스냅샷 상태 (pending, completed 등)
						snap.getStartTime().toString());  // 스냅샷 생성 시작 시간
			}
		}
	}

	// 특정 스냅샷을 삭제하는 메서드
	public static void deleteSnapshot(String snapshotId) {
		System.out.printf("Deleting snapshot %s\n", snapshotId);
		// 스냅샷 삭제 요청 객체 생성
		DeleteSnapshotRequest request = new DeleteSnapshotRequest().withSnapshotId(snapshotId);
		// AWS EC2 클라이언트를 통해 삭제 요청 실행
		ec2.deleteSnapshot(request);
		System.out.println("Snapshot deleted successfully.");
	}

	// 특정 스냅샷을 기반으로 EBS 볼륨을 생성하는 메서드
	public static void createVolumeFromSnapshot(String snapshotId, String availabilityZone) {
		System.out.printf("Creating volume from snapshot %s in AZ %s\n", snapshotId, availabilityZone);
		// EBS 볼륨 생성 요청 객체 생성
		CreateVolumeRequest request = new CreateVolumeRequest()
				.withSnapshotId(snapshotId)
				.withAvailabilityZone(availabilityZone);
		// AWS EC2 클라이언트를 통해 볼륨 생성 요청 실행
		Volume volume = ec2.createVolume(request).getVolume();
		System.out.printf("Created volume from snapshot: %s\n", volume.getVolumeId());
	}
}
	