package run

import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import workload.PatternRequest
import java.net.ConnectException

class Workerhandler {

    suspend fun getWorkerStatus(worker: Worker) : String {
        var url:String = buildURL(worker, "/api/getStatus")
        var answer:String = "Error"
        //Make call to worker
        try {
            var client = HttpClient()
            answer = client.get<String>(url)
            client.close()
        } catch (e: ConnectException) {
            answer="Error: Connect exception while connecting to " + url
        }
        return answer
    }

    suspend fun clearWorker(worker: Worker) : Boolean {
        try {
            var client = HttpClient()
            var url = buildURL(worker, "/api/clear")
            val response = client.get<String>(url)
            client.close()
            if (response == "OK") {
                return true
            } else {
                println("Error while clearing worker " + worker.id + ", " + worker.url + ": " + response)
            }
        } catch (e:ConnectException) {
            println("Error while clearing worker " + worker.id + ", " + worker.url + ": " + e.toString())
        }
        return false
    }

    suspend fun setListener(worker: Worker, workersetID: Int) : Boolean {
        try {
            var client = HttpClient()
            var url = buildURL(worker, "/api/setListener")
            val response = client.put<String>(url, {
                body = "http://localhost:8080/api/run/notification/" + workersetID + "/" + worker.id
            })
            client.close()
            if (response == "OK") {
                return true
            } else {
                println("Error while setListener for worker " + worker.id + ", " + worker.url + ": " + response)
            }
        } catch (e:ConnectException) {
            println("Error while setListener for worker " + worker.id + ", " + worker.url + ": " + e.toString())
        }
        return false
    }

    suspend fun setEndpoint(worker: Worker, endpoint:String) : Boolean {
        try {
            var client = HttpClient()
            var url = buildURL(worker, "/api/setEndpoint")
            val response = client.put<String>(url, {
                body = endpoint
            })
            client.close()
            if (response == "OK") {
                return true
            } else {
                println("Error while setEndpoint for worker " + worker.id + ", " + worker.url + ": " + response)
            }
        } catch (e:ConnectException) {
            println("Error while setEndpoint for worker " + worker.id + ", " + worker.url + ": " + e.toString())
        }
        return false
    }

    suspend fun setThreads(worker: Worker, threads:Int) : Boolean {
        try {
            var client = HttpClient()
            var url = buildURL(worker, "/api/setThreads")
            val response = client.put<String>(url, {
                body = threads.toString()
            })
            client.close()
            if (response == "OK") {
                return true
            } else {
                println("Error while setThreads for worker " + worker.id + ", " + worker.url + ": " + response)
            }
        } catch (e:ConnectException) {
            println("Error while setThreads for worker " + worker.id + ", " + worker.url + ": " + e.toString())
        }
        return false
    }



    suspend fun ensureAllWorkerStatus(workers : MutableMap<Int, Worker>, status: String) : Boolean {
        if (workers.values.size == 0) {
            println("No workers given")
            return false
        }
        var allStatus = true;

        for (w in workers.values) {
            val response = getWorkerStatus(w)
            if (response != status) {
                println("Worker " + w.id + " is not " + status + " but " + response)
                allStatus = false
            }
        }
        return allStatus
    }

    suspend fun distributeWorkload(workers: MutableMap<Int, Worker>, workload:Array<PatternRequest>) : Boolean{
        if (workers.values.size == 0) {
            println("No workers given")
            return false
        }
        var packageNumber = workers.values.size
        var packages: ArrayList<ArrayList<PatternRequest>> = ArrayList()

        //Create empty packages
        for (w in workers.values) {
            packages.add(ArrayList())
        }

        //Assign PatternRequests to Packages
        var nextIdx = 0
        for (req in workload) {
            packages.get(nextIdx).add(req)
            nextIdx++
            if (nextIdx >= packages.size) {
                nextIdx = 0
            }
        }

        //Send packages to workers
        for (w in workers.values) {
            var pack = packages.removeAt(0)
            try {
                var client = HttpClient()
                var url = buildURL(w, "/api/setWorkload")
                val response = client.put<String>(url, {
                    body = GsonBuilder().create().toJson(pack)
                })
                client.close()
                if (response == "OK") {
                    println("Send workload (" + pack.size + "items) to worker " + w.id + ", " + w.url)
                } else {
                    println("Error while setWorkload for worker " + w.id + ", " + w.url + ": " + response)
                    return false
                }
            } catch (e:ConnectException) {
                println("Error while setWorkload for worker " + w.id + ", " + w.url + ": " + e.toString())
                return false
            }
        }

        return true
    }

    suspend fun startBenchmark(workers: MutableMap<Int, Worker>) : Boolean{
        if (workers.values.size == 0) {
            println("No workers given")
            return false
        }

        //Send packages to workers
        for (w in workers.values) {
            try {
                var client = HttpClient()
                var url = buildURL(w, "/api/startBenchmark")
                val response = client.get<String>(url)
                client.close()
                if (response == "OK") {
                    println("Benchmarked started at worker " + w.id + ", " + w.url)
                } else {
                    println("Error while starting benchmark run for worker " + w.id + ", " + w.url + ": " + response)
                    return false
                }
            } catch (e:ConnectException) {
                println("Error while starting benchmark run for worker " + w.id + ", " + w.url + ": " + e.toString())
                return false
            }
        }

        return true
    }

    private fun buildURL(w: Worker, path:String) : String{
        var url:String = w.url + path
        if (!url.startsWith("http://")) {
            url = "http://" + url
        }
        return url
    }

}