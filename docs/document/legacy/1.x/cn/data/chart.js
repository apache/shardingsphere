var chartData = {
    "compareQuery": {
        labels: ["50", "100", "200", "300", "600"],
        datasets: [
           {
                label: "JDBC",
                data: [1812, 3828, 5217, 7239, 7169]
            }, {
                label: "Sharding-JDBC",
                data: [1591, 2547, 5062, 7284, 7151]
            }
        
        ]
    },"compareInsert": {
        labels: ["50", "100", "200", "300", "600"],
        datasets: [
            {
                label: "JDBC",
                data: [2520, 5860, 6420, 6200, 6250]
            },{
                label: "Sharding-JDBC",
                data: [2410, 4230, 5445, 5603, 5642]
            }
        
        ]
    },"compareUpdate": {
        labels: ["50", "100", "200", "300", "600"],
        datasets: [
            {
                label: "JDBC",
                data: [2430, 5712, 6557, 7773, 7561]
            },{
                label: "Sharding-JDBC",
                data: [2059, 2715, 4770, 7280, 7210]
            }
        
        ]
    }, "singleAndDubbleQuery": {
        labels: ["50", "100", "200", "300", "600"],
        datasets: [
            {
                label: "双库",
                data: [3724, 6246, 11480, 13107, 13960]
            },{
                label: "单库",
                data: [1591, 2547, 5062, 7284, 7151]
            }
        
        ]
    },"singleAndDubbleInsert": {
        labels: ["50", "100", "200", "300", "600"],
        datasets: [
            {
                label: "双库",
                data: [4021, 6807, 7911, 8109, 7619]
            },{
                label: "单库",
                data: [2410, 4230, 5445, 5603, 5642]
            }
        
        ]
    },"singleAndDubbleUpdate": {
        labels: ["50", "100", "200", "300", "600"],
        datasets: [
            {
                label: "双库",
                data: [2190, 4464, 9039, 10144, 11970]
            },{
                label: "单库",
                data: [2059, 2715, 4770, 7280, 7210]
            }
        ]
    },"fatigueTest": {
        labels: ["0", "1小时", "2小时", "3小时", "4小时", "5小时", "6小时", "7小时", "8小时"],
        datasets: [
            {
                label: "jvm堆大小",
                data: [0, 567, 533, 587, 523, 546, 577 ,534,577]
            }
        ]
    }
};

var charStyle = [
    
    {
        backgroundColor: "rgba(246,179,107,0.2)",
        borderColor: "rgba(246,179,107,1)",
        pointBorderColor: "rgba(246,179,107,1)",
        pointBackgroundColor: "#fff",
        pointBorderWidth: 1,
        pointHoverRadius: 5,
        pointHoverBackgroundColor: "rgba(246,179,107,1)",
        pointHoverBorderColor: "rgba(246,179,107,1)",
        pointHoverBorderWidth: 2
    },
    {
        backgroundColor: "rgba(61,134,198,0.2)",
        borderColor: "rgba(61,134,198,1)",
        pointBorderColor: "rgba(61,134,198,1)",
        pointBackgroundColor: "#fff",
        pointBorderWidth: 1,
        pointHoverRadius: 5,
        pointHoverBackgroundColor: "rgba(61,134,198,1)",
        pointHoverBorderColor: "rgba(61,134,198,1)",
        pointHoverBorderWidth: 2
    }
];