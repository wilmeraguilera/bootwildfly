
node{

	//general variables for the process
	def statusProcess = "Proceso Exitoso"
	def errorMessage = ""

	try {
		
		def mvnCmd
		def mvnHome
		def groupId
		def artifactId
		def version
		def devTag
		def prodTag

		//Stage for configuration of the pipeline
		stage('Preparing'){
			mvnHome = tool 'M2_3.6.2'

			// Define Maven Command. Make sure it points to the correct
			// settings for our Nexus installation (use the service to
			// bypass the router). The file settings.xml
			// needs to be in the Source Code repository.
			mvnCmd = "${mvnHome}/bin/mvn -s ./settings.xml"

			env.JAVA_HOME=tool 'JDK18'
			env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
			sh 'java -version'
			
			
		}

		//Stage para obtener el código fuente del repositorio GIT
		stage('Checkout'){
			echo "Checkout Source"
			git branch: 'develop', url: 'https://github.com/wilmeraguilera/bootwildfly.git'

			groupId    = getGroupIdFromPom("pom.xml")
			artifactId = getArtifactIdFromPom("pom.xml")
			version    = getVersionFromPom("pom.xml")

			echo groupId
			echo artifactId
			echo version
			
			// Set the tag for the development image: version + build number
			devTag  = "${version}-" + currentBuild.number
			// Set the tag for the production image: version
			prodTag = "${version}"
		}

		// Using Maven build the war file
		// Do not run tests in this step
		stage('Build') {
			echo "Init Building package"
			sh "${mvnCmd} clean package -DskipTests"
			echo "End Building package"
		}
		
		
		//Stage for execution Unit Test
		stage('Run Unit Test') {
			echo "Init Unit Test"
			// TBD
			sh "${mvnCmd} test"
			echo "End Unit Test"
		}

		// Using Maven call SonarQube for Code Analysis
        stage('SonarQube Scan') {
			echo "Init Running Code Analysis"
              
  			withSonarQubeEnv('sonar') {
  				sh "${mvnCmd} sonar:sonar " +
  				"-Dsonar.junit.reportsPath=target/surefire-reports -Dsonar.jacoco.reportPath=target/jacoco.exec "
  			}
			
			sleep(10)
			
			timeout(time: 1, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
            }
			
            echo "End Running Code Analysis"
        }
		
		
		//Public in repository
		stage('Publish to Nexus') {
			echo "Publish to Nexus"
			// TBD
			sh "${mvnCmd} deploy -DskipTests=true -DaltDeploymentRepository=nexus::default::http://nexus3-nexus.192.168.42.220.nip.io/repository/maven-releases/ "+
			"-DaltSnapshotDeploymentRepository=nexus::default::http://nexus3-nexus.192.168.42.220.nip.io/repository/maven-snapshots/"
		}
		
		stage('Create Image'){
			echo devTag
			echo prodTag
			
			openshift.withCluster() {
				openshift.withProject("calculadora-dev") {
				  openshift.selector("bc", "calculadora").startBuild("--from-file=./target/ROOT.war", "--wait=true")
		
				  // OR use the file you just published into Nexus:
				  // "--from-file=http://nexus3.${prefix}-nexus.svc.cluster.local:8081/repository/releases/org/jboss/quickstarts/eap/tasks/${version}/tasks-${version}.war"
				  openshift.tag("calculadora:latest", "calculadora:${devTag}")
				}
			  }
		}
		
		stage('Deploy to DEV'){
			openshift.withCluster() {
				openshift.withProject("calculadora-dev") {
					openshift.set("image", "dc/calculadora", "calculadora=172.30.1.1:5000/calculadora-dev/calculadora:${devTag}")
					
					// Deploy the development application.
					openshift.selector("dc", "calculadora").rollout().latest();
		  
					// Wait for application to be deployed
					def dc = openshift.selector("dc", "calculadora").object()
					def dc_version = dc.status.latestVersion
					echo "La ultima version es: "+dc_version
					def rc = openshift.selector("rc", "calculadora-${dc_version}").object()
		  
					echo "Waiting for ReplicationController calculadora-${dc_version} to be ready"
					while (rc.spec.replicas != rc.status.readyReplicas) {
					  sleep 5
					  rc = openshift.selector("rc", "calculadora-${dc_version}").object()
					}
					
				}
			}
		}
		
		// Run Owasp ZAP Scan
		stage('Owasp ZAP Scan') {
			echo "Owasp ZAP Scan";
		}
		
		//Deploy to QA
		stage('Deploy to QA') {
			input "Deploy to QA?"
			
			openshift.withCluster() {
				openshift.withProject("calculadora-qa") {
					openshift.set("image", "dc/calculadora", "calculadora=172.30.1.1:5000/calculadora-dev/calculadora:${devTag}")

					// Deploy the qa application.
					openshift.selector("dc", "calculadora").rollout().latest();
					// Wait for application to be deployed
					def dc = openshift.selector("dc", "calculadora").object()
					def dc_version = dc.status.latestVersion
					echo "La ultima version es: "+dc_version
					def rc = openshift.selector("rc", "calculadora-${dc_version}").object()
					
					echo "Waiting for ReplicationController calculadora-${dc_version} to be ready"
					while (rc.spec.replicas != rc.status.readyReplicas) {
						echo "rc.spec.replicas: "+rc.spec.replicas
						echo "rc.status.readyReplicas: "+rc.status.readyReplicas
						sleep 5
						rc = openshift.selector("rc", "calculadora-${dc_version}").object()
					}

				}
			}
		}
       
    }catch(e){
        statusProcess = "Proceso con error"
        errorMessage = e.toString()
        throw e
    }finally{
        //emailext(mimeType: 'text/html', replyTo: 'waguilera@redhat.com', subject: statusProcess+" : " + env.JOB_NAME, to: 'waguilera@redhat.com', body: statusProcess + " : " + env.JOB_NAME+ " : "+errorMessage)

    }


}

// Convenience Functions to read variables from the pom.xml
// Do not change anything below this line.
// --------------------------------------------------------
def getVersionFromPom(pom) {
    def matcher = readFile(pom) =~ '<version>(.+)</version>'
    matcher ? matcher[0][1] : null
}
def getGroupIdFromPom(pom) {
    def matcher = readFile(pom) =~ '<groupId>(.+)</groupId>'
    matcher ? matcher[0][1] : null
}
def getArtifactIdFromPom(pom) {
    def matcher = readFile(pom) =~ '<artifactId>(.+)</artifactId>'
    matcher ? matcher[0][1] : null
}
